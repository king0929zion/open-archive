package com.king0929zion.openarchive.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        EntryEntity::class,
        EntryImageEntity::class,
        CommentEntity::class,
        ProviderEntity::class,
        ProviderModelEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class OpenArchiveDatabase : RoomDatabase() {
    abstract fun archiveDao(): ArchiveDao
    abstract fun providerDao(): ProviderDao
}
