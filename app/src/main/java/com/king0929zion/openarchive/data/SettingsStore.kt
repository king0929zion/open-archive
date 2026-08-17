package com.king0929zion.openarchive.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.settingsDataStore by preferencesDataStore("open_archive_settings")

class SettingsStore(private val context: Context) {
    private object Keys {
        val UserName = stringPreferencesKey("user_name")
        val AvatarSeed = stringPreferencesKey("avatar_seed")
        val DefaultProvider = stringPreferencesKey("default_provider")
        val DefaultModel = stringPreferencesKey("default_model")
        val Draft = stringPreferencesKey("draft_json")
    }

    val userName: Flow<String> = context.settingsDataStore.data.map { it[Keys.UserName] ?: "Serein" }
    val avatarSeed: Flow<String> = context.settingsDataStore.data.map { it[Keys.AvatarSeed] ?: "Serein" }
    val defaultProviderId: Flow<String> = context.settingsDataStore.data.map { it[Keys.DefaultProvider] ?: "" }
    val defaultModelId: Flow<String> = context.settingsDataStore.data.map { it[Keys.DefaultModel] ?: "" }
    val draft: Flow<DraftSnapshot> = context.settingsDataStore.data.map { prefs ->
        prefs[Keys.Draft]?.let(::decodeDraft) ?: DraftSnapshot()
    }

    suspend fun setDefaultModel(providerId: String, modelId: String) {
        context.settingsDataStore.edit {
            it[Keys.DefaultProvider] = providerId
            it[Keys.DefaultModel] = modelId
        }
    }

    suspend fun clearDefaultModel() = setDefaultModel("", "")

    suspend fun saveDraft(draft: DraftSnapshot) {
        context.settingsDataStore.edit { it[Keys.Draft] = encodeDraft(draft) }
    }

    suspend fun clearDraft() = saveDraft(DraftSnapshot())

    private fun encodeDraft(d: DraftSnapshot): String = JSONObject().apply {
        put("text", d.text)
        put("location", d.location)
        put("weather", d.weather)
        put("mood", d.mood)
        put("images", JSONArray(d.images))
    }.toString()

    private fun decodeDraft(raw: String): DraftSnapshot = runCatching {
        val o = JSONObject(raw)
        val a = o.optJSONArray("images") ?: JSONArray()
        DraftSnapshot(
            text = o.optString("text"),
            images = List(a.length()) { index -> a.optString(index) }.filter { it.isNotBlank() },
            location = o.optString("location"),
            weather = o.optString("weather"),
            mood = o.optString("mood"),
        )
    }.getOrDefault(DraftSnapshot())
}
