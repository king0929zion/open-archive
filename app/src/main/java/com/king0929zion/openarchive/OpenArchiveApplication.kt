package com.king0929zion.openarchive

import android.app.Application
import androidx.room.Room
import com.king0929zion.openarchive.ai.MultiProviderAiClient
import com.king0929zion.openarchive.data.ArchiveRepository
import com.king0929zion.openarchive.data.OpenArchiveDatabase
import com.king0929zion.openarchive.data.ProviderRepository
import com.king0929zion.openarchive.data.SettingsStore
import com.king0929zion.openarchive.security.ApiKeyCipher
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class OpenArchiveApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        val db = Room.databaseBuilder(
            this,
            OpenArchiveDatabase::class.java,
            "open-archive.db",
        ).build()
        val http = OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(0, TimeUnit.SECONDS)
            .build()
        container = AppContainer(
            archiveRepository = ArchiveRepository(db.archiveDao()),
            providerRepository = ProviderRepository(db.providerDao()),
            settingsStore = SettingsStore(this),
            apiKeyCipher = ApiKeyCipher(),
            aiClient = MultiProviderAiClient(http),
        )
    }
}

data class AppContainer(
    val archiveRepository: ArchiveRepository,
    val providerRepository: ProviderRepository,
    val settingsStore: SettingsStore,
    val apiKeyCipher: ApiKeyCipher,
    val aiClient: MultiProviderAiClient,
)
