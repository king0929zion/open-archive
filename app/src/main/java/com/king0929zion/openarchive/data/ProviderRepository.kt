package com.king0929zion.openarchive.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class ProviderRepository(private val dao: ProviderDao) {
    val providers: Flow<List<AiProvider>> = combine(
        dao.observeProviders(),
        dao.observeModels(),
    ) { providers, models ->
        val byProvider = models.groupBy { it.providerId }
        providers.map { p ->
            AiProvider(
                id = p.id,
                name = p.name,
                format = p.format,
                baseUrl = p.baseUrl,
                encryptedApiKey = p.encryptedApiKey,
                models = byProvider[p.id].orEmpty().map {
                    ProviderModel(it.providerId, it.modelId, it.displayName, it.vision)
                },
            )
        }
    }

    suspend fun save(provider: AiProvider) {
        dao.replaceProvider(
            ProviderEntity(
                id = provider.id,
                name = provider.name,
                format = provider.format,
                baseUrl = provider.baseUrl.trimEnd('/'),
                encryptedApiKey = provider.encryptedApiKey,
            ),
            provider.models.map {
                ProviderModelEntity(
                    providerId = provider.id,
                    modelId = it.modelId,
                    displayName = it.displayName,
                    vision = it.vision,
                )
            },
        )
    }

    suspend fun delete(id: String) = dao.deleteProvider(id)
}
