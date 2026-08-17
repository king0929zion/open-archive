package com.king0929zion.openarchive.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "entries")
data class EntryEntity(
    @PrimaryKey val id: String,
    val createdAt: Long,
    val text: String,
    val location: String,
    val weather: String,
    val mood: String,
)

@Entity(
    tableName = "entry_images",
    foreignKeys = [ForeignKey(
        entity = EntryEntity::class,
        parentColumns = ["id"],
        childColumns = ["entryId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("entryId")],
)
data class EntryImageEntity(
    @PrimaryKey val id: String,
    val entryId: String,
    val uri: String,
    val sortOrder: Int,
)

@Entity(
    tableName = "comments",
    foreignKeys = [ForeignKey(
        entity = EntryEntity::class,
        parentColumns = ["id"],
        childColumns = ["entryId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("entryId"), Index("parentId")],
)
data class CommentEntity(
    @PrimaryKey val id: String,
    val entryId: String,
    val parentId: String?,
    val authorName: String,
    val avatarSeed: String,
    val text: String,
    val createdAt: Long,
)

@Entity(tableName = "providers")
data class ProviderEntity(
    @PrimaryKey val id: String,
    val name: String,
    val format: String,
    val baseUrl: String,
    val encryptedApiKey: String,
)

@Entity(
    tableName = "provider_models",
    primaryKeys = ["providerId", "modelId"],
    foreignKeys = [ForeignKey(
        entity = ProviderEntity::class,
        parentColumns = ["id"],
        childColumns = ["providerId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("providerId")],
)
data class ProviderModelEntity(
    val providerId: String,
    val modelId: String,
    val displayName: String,
    val vision: Boolean,
)
