package com.king0929zion.openarchive.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ArchiveDao {
    @Query("SELECT * FROM entries ORDER BY createdAt DESC")
    fun observeEntries(): Flow<List<EntryEntity>>

    @Query("SELECT * FROM entry_images ORDER BY entryId, sortOrder")
    fun observeImages(): Flow<List<EntryImageEntity>>

    @Query("SELECT * FROM comments ORDER BY createdAt ASC")
    fun observeComments(): Flow<List<CommentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: EntryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImages(images: List<EntryImageEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: CommentEntity)

    @Query("DELETE FROM entries WHERE id = :entryId")
    suspend fun deleteEntryById(entryId: String)

    @Query("SELECT COUNT(*) FROM entries")
    suspend fun entryCount(): Int

    @Transaction
    suspend fun insertEntryWithImages(entry: EntryEntity, images: List<EntryImageEntity>) {
        insertEntry(entry)
        if (images.isNotEmpty()) insertImages(images)
    }
}
