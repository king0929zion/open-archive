package com.king0929zion.openarchive.data

data class ArchiveEntry(
    val id: String,
    val createdAt: Long,
    val text: String,
    val images: List<String> = emptyList(),
    val location: String = "",
    val weather: String = "",
    val mood: String = "",
)

data class ArchiveComment(
    val id: String,
    val entryId: String,
    val parentId: String? = null,
    val authorName: String,
    val avatarSeed: String,
    val text: String,
    val createdAt: Long,
)

data class ProviderModel(
    val providerId: String,
    val modelId: String,
    val displayName: String = "",
    val vision: Boolean = false,
)

data class AiProvider(
    val id: String,
    val name: String,
    val format: String,
    val baseUrl: String,
    val encryptedApiKey: String,
    val models: List<ProviderModel> = emptyList(),
)

data class DraftSnapshot(
    val text: String = "",
    val images: List<String> = emptyList(),
    val location: String = "",
    val weather: String = "",
    val mood: String = "",
)

data class NearbyPlace(
    val value: String,
    val name: String,
    val category: String,
    val distance: String,
    val address: String,
)
