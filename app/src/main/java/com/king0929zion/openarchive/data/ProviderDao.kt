package com.king0929zion.openarchive.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ProviderDao {
    @Query("SELECT * FROM providers ORDER BY name COLLATE NOCASE")
    fun observeProviders(): Flow<List<ProviderEntity>>

    @Query("SELECT * FROM provider_models ORDER BY providerId, modelId COLLATE NOCASE")
    fun observeModels(): Flow<List<ProviderModelEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProvider(provider: ProviderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertModels(models: List<ProviderModelEntity>)

    @Query("DELETE FROM provider_models WHERE providerId = :providerId")
    suspend fun clearModels(providerId: String)

    @Query("DELETE FROM providers WHERE id = :providerId")
    suspend fun deleteProvider(providerId: String)

    @Transaction
    suspend fun replaceProvider(provider: ProviderEntity, models: List<ProviderModelEntity>) {
        upsertProvider(provider)
        clearModels(provider.id)
        if (models.isNotEmpty()) upsertModels(models)
    }
}
