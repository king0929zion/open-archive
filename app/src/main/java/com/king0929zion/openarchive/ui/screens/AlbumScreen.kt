package com.king0929zion.openarchive.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.king0929zion.openarchive.ArchiveViewModel
import com.king0929zion.openarchive.ui.components.ArchiveHeader
import com.king0929zion.openarchive.ui.components.DemoOrRemoteImage
import com.king0929zion.openarchive.ui.theme.ArchiveColors

@Composable
fun AlbumScreen(viewModel: ArchiveViewModel, onBack: () -> Unit, onEntry: (String) -> Unit) {
    val entries by viewModel.entries.collectAsStateWithLifecycle()
    val photos = remember(entries) {
        entries.flatMap { entry -> entry.images.map { uri -> entry.id to uri } }
    }
    Column(Modifier.fillMaxSize().background(Color.White)) {
        ArchiveHeader(title = "相册", onBack = onBack)
        if (photos.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("还没有照片", color = ArchiveColors.Tertiary, fontSize = 12.sp)
            }
        } else {
            Text("共 ${photos.size} 张照片", color = ArchiveColors.Tertiary, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 22.dp, vertical = 8.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize().padding(horizontal = 22.dp),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(3.dp),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(3.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 40.dp),
            ) {
                items(photos, key = { it.first + it.second }, contentType = { "photo" }) { (entryId, uri) ->
                    DemoOrRemoteImage(
                        uri,
                        Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(10.dp)).clickable { onEntry(entryId) }
                    )
                }
            }
        }
    }
}
