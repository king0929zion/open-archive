package com.king0929zion.openarchive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.king0929zion.openarchive.ai.AiChatMessage
import com.king0929zion.openarchive.ai.MultiProviderAiClient
import com.king0929zion.openarchive.data.AiProvider
import com.king0929zion.openarchive.data.ArchiveComment
import com.king0929zion.openarchive.data.ArchiveEntry
import com.king0929zion.openarchive.data.ArchiveRepository
import com.king0929zion.openarchive.data.DraftSnapshot
import com.king0929zion.openarchive.data.ProviderModel
import com.king0929zion.openarchive.data.ProviderRepository
import com.king0929zion.openarchive.data.SettingsStore
import com.king0929zion.openarchive.security.ApiKeyCipher
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class ArchiveViewModel(
    private val archiveRepository: ArchiveRepository,
    private val providerRepository: ProviderRepository,
    private val settingsStore: SettingsStore,
    private val apiKeyCipher: ApiKeyCipher,
    private val aiClient: MultiProviderAiClient,
) : ViewModel() {
    val entries: StateFlow<List<ArchiveEntry>> = archiveRepository.entries.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList()
    )
    val comments: StateFlow<List<ArchiveComment>> = archiveRepository.comments.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList()
    )
    val providers: StateFlow<List<AiProvider>> = providerRepository.providers.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList()
    )
    val userName = settingsStore.userName.stateIn(viewModelScope, SharingStarted.Eagerly, "Serein")
    val avatarSeed = settingsStore.avatarSeed.stateIn(viewModelScope, SharingStarted.Eagerly, "Serein")
    val defaultProviderId = settingsStore.defaultProviderId.stateIn(viewModelScope, SharingStarted.Eagerly, "")
    val defaultModelId = settingsStore.defaultModelId.stateIn(viewModelScope, SharingStarted.Eagerly, "")

    private val _draft = MutableStateFlow(DraftSnapshot())
    val draft: StateFlow<DraftSnapshot> = _draft.asStateFlow()
    private var draftPersistJob: Job? = null

    private val _achiMessages = MutableStateFlow(
        listOf(AchiUiMessage("welcome", false, "嗨，我是 Achi。你可以问我关于这些记录的任何事。"))
    )
    val achiMessages = _achiMessages.asStateFlow()
    private val _achiStreaming = MutableStateFlow(false)
    val achiStreaming = _achiStreaming.asStateFlow()
    private var achiJob: Job? = null

    init {
        viewModelScope.launch { archiveRepository.seedDemoIfEmpty() }
        viewModelScope.launch { _draft.value = settingsStore.draft.first() }
        viewModelScope.launch {
            combine(providers, defaultProviderId, defaultModelId) { p, dp, dm -> Triple(p, dp, dm) }
                .collect { (list, providerId, modelId) ->
                    val valid = list.any { it.id == providerId && it.models.any { m -> m.modelId == modelId } }
                    if (!valid) {
                        val fallback = list.firstNotNullOfOrNull { provider ->
                            provider.models.firstOrNull()?.let { provider.id to it.modelId }
                        }
                        if (fallback != null) settingsStore.setDefaultModel(fallback.first, fallback.second)
                        else if (providerId.isNotBlank() || modelId.isNotBlank()) settingsStore.clearDefaultModel()
                    }
                }
        }
    }

    fun entry(id: String): ArchiveEntry? = entries.value.firstOrNull { it.id == id }
    fun commentsFor(entryId: String): List<ArchiveComment> = comments.value.filter { it.entryId == entryId }

    fun updateDraft(transform: (DraftSnapshot) -> DraftSnapshot) {
        val next = transform(_draft.value)
        _draft.value = next
        draftPersistJob?.cancel()
        draftPersistJob = viewModelScope.launch {
            delay(250)
            settingsStore.saveDraft(next)
        }
    }

    fun addImages(uris: List<String>) = updateDraft { current ->
        current.copy(images = (current.images + uris).distinct().take(9))
    }

    fun removeImage(index: Int) = updateDraft { current ->
        current.copy(images = current.images.filterIndexed { i, _ -> i != index })
    }

    fun publish(onSaved: (String) -> Unit = {}) {
        val current = _draft.value
        if (current.text.isBlank() && current.images.isEmpty()) return
        viewModelScope.launch {
            draftPersistJob?.cancel()
            val id = archiveRepository.createEntry(current)
            _draft.value = DraftSnapshot()
            settingsStore.clearDraft()
            onSaved(id)
        }
    }

    fun deleteEntry(id: String, done: () -> Unit = {}) {
        viewModelScope.launch { archiveRepository.deleteEntry(id); done() }
    }

    fun addComment(entryId: String, text: String, parentId: String? = null) {
        if (text.isBlank()) return
        viewModelScope.launch {
            archiveRepository.addComment(entryId, text, userName.value, avatarSeed.value, parentId)
        }
    }

    fun setDefaultModel(providerId: String, modelId: String) {
        viewModelScope.launch { settingsStore.setDefaultModel(providerId, modelId) }
    }

    fun saveProvider(
        id: String?,
        name: String,
        format: String,
        baseUrl: String,
        apiKey: String,
        keepExistingEncryptedKey: String = "",
        models: List<ProviderModel>,
        done: () -> Unit = {},
    ) {
        if (name.isBlank() || baseUrl.isBlank()) return
        viewModelScope.launch {
            val encrypted = when {
                apiKey.isNotBlank() -> apiKeyCipher.encrypt(apiKey)
                keepExistingEncryptedKey.isNotBlank() -> keepExistingEncryptedKey
                else -> ""
            }
            val providerId = id ?: "p-${UUID.randomUUID()}"
            providerRepository.save(
                AiProvider(
                    id = providerId,
                    name = name.trim(),
                    format = format,
                    baseUrl = baseUrl.trim().trimEnd('/'),
                    encryptedApiKey = encrypted,
                    models = models.map { it.copy(providerId = providerId) },
                )
            )
            if (defaultProviderId.value.isBlank() && models.isNotEmpty()) {
                settingsStore.setDefaultModel(providerId, models.first().modelId)
            }
            done()
        }
    }

    fun deleteProvider(id: String, done: () -> Unit = {}) {
        viewModelScope.launch { providerRepository.delete(id); done() }
    }

    fun decryptProviderKey(provider: AiProvider?): String =
        provider?.let { apiKeyCipher.decrypt(it.encryptedApiKey) }.orEmpty()

    fun fetchModels(
        format: String,
        baseUrl: String,
        apiKey: String,
        existingEncryptedKey: String,
        onResult: (Result<List<String>>) -> Unit,
    ) {
        viewModelScope.launch {
            val key = if (apiKey.isNotBlank()) apiKey else apiKeyCipher.decrypt(existingEncryptedKey)
            val temp = AiProvider("temp", "temp", format, baseUrl.trimEnd('/'), "")
            onResult(runCatching { aiClient.listModels(temp, key) })
        }
    }

    fun sendAchi(text: String) {
        if (text.isBlank() || _achiStreaming.value) return
        val provider = providers.value.firstOrNull { it.id == defaultProviderId.value }
        val model = provider?.models?.firstOrNull { it.modelId == defaultModelId.value }
        if (provider == null || model == null) {
            _achiMessages.value += AchiUiMessage(UUID.randomUUID().toString(), false, "还没有设置默认模型。请先到设置里配置供应商和模型。")
            return
        }
        val user = AchiUiMessage(UUID.randomUUID().toString(), true, text.trim())
        val answerId = UUID.randomUUID().toString()
        _achiMessages.value += user
        _achiMessages.value += AchiUiMessage(answerId, false, "")
        achiJob?.cancel()
        achiJob = viewModelScope.launch {
            _achiStreaming.value = true
            val recent = entries.value.take(12).joinToString("\n\n") { e ->
                val meta = listOfNotNull(
                    e.location.takeIf { it.isNotBlank() },
                    weatherLabel(e.weather).takeIf { it.isNotBlank() },
                    moodLabel(e.mood).takeIf { it.isNotBlank() },
                ).joinToString(" · ")
                "- ${e.text}${if (meta.isNotBlank()) "\n  $meta" else ""}"
            }
            val prompt = """
                你是 Achi，是 Open Archive 中克制、自然的个人记录助手。
                以下是用户最近的部分 Archive，仅在问题相关时引用，不要捏造不存在的记录：
                $recent
            """.trimIndent()
            val key = apiKeyCipher.decrypt(provider.encryptedApiKey)
            try {
                aiClient.streamChat(
                    provider = provider,
                    apiKey = key,
                    modelId = model.modelId,
                    messages = listOf(
                        AiChatMessage("system", prompt),
                        AiChatMessage("user", text.trim()),
                    ),
                ).collect { delta ->
                    _achiMessages.value = _achiMessages.value.map {
                        if (it.id == answerId) it.copy(text = it.text + delta) else it
                    }
                }
            } catch (_: CancellationException) {
                // User-initiated stop: keep the partial answer without turning it into an error.
            } catch (error: Throwable) {
                _achiMessages.value = _achiMessages.value.map {
                    if (it.id == answerId) it.copy(text = "请求失败：${error.message ?: "未知错误"}") else it
                }
            } finally {
                _achiStreaming.value = false
            }
        }
    }

    fun stopAchi() {
        achiJob?.cancel()
        _achiStreaming.value = false
    }

    companion object {
        fun weatherLabel(key: String) = when (key) {
            "sunny" -> "晴朗"; "cloudy" -> "多云"; "overcast" -> "阴天"; "rain" -> "小雨"; "snow" -> "下雪"; else -> ""
        }
        fun moodLabel(key: String) = when (key) {
            "low" -> "低落"; "calm" -> "平静"; "cozy" -> "悠闲"; "happy" -> "开心"; "energy" -> "活力"; else -> ""
        }
    }
}

data class AchiUiMessage(val id: String, val fromUser: Boolean, val text: String)

class ArchiveViewModelFactory(private val container: AppContainer) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = ArchiveViewModel(
        archiveRepository = container.archiveRepository,
        providerRepository = container.providerRepository,
        settingsStore = container.settingsStore,
        apiKeyCipher = container.apiKeyCipher,
        aiClient = container.aiClient,
    ) as T
}
