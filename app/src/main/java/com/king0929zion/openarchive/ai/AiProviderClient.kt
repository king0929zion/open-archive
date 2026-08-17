package com.king0929zion.openarchive.ai

import com.king0929zion.openarchive.data.AiProvider
import kotlinx.coroutines.flow.Flow

data class AiChatMessage(val role: String, val text: String)

data class ProviderConnectionResult(
    val ok: Boolean,
    val message: String,
)

interface AiProviderClient {
    suspend fun listModels(provider: AiProvider, apiKey: String): List<String>
    fun streamChat(
        provider: AiProvider,
        apiKey: String,
        modelId: String,
        messages: List<AiChatMessage>,
    ): Flow<String>
}
