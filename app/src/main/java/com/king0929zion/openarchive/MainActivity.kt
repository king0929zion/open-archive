package com.king0929zion.openarchive

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.king0929zion.openarchive.ui.OpenArchiveApp
import com.king0929zion.openarchive.ui.theme.OpenArchiveTheme

class MainActivity : ComponentActivity() {
    private val viewModel: ArchiveViewModel by viewModels {
        ArchiveViewModelFactory((application as OpenArchiveApplication).container)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OpenArchiveTheme {
                OpenArchiveApp(viewModel)
            }
        }
    }
}
