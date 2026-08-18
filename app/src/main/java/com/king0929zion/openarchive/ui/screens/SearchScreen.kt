package com.king0929zion.openarchive.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.king0929zion.openarchive.ArchiveViewModel
import com.king0929zion.openarchive.data.ArchiveEntry
import com.king0929zion.openarchive.ui.ArchiveFormatters
import com.king0929zion.openarchive.ui.components.ArchiveHeader
import com.king0929zion.openarchive.ui.components.DemoOrRemoteImage
import com.king0929zion.openarchive.ui.icons.ArchiveIcons
import com.king0929zion.openarchive.ui.theme.ArchiveColors
import kotlinx.coroutines.delay

private data class IndexedArchiveEntry(val entry: ArchiveEntry, val haystack: String)

@Composable
fun SearchScreen(
    viewModel: ArchiveViewModel,
    onBack: () -> Unit,
    onEntry: (String) -> Unit,
) {
    val entries by viewModel.entries.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf("") }
    var settledQuery by remember { mutableStateOf("") }

    LaunchedEffect(query) {
        if (query.isBlank()) settledQuery = ""
        else {
            delay(90)
            settledQuery = query
        }
    }

    // Build the expensive normalized/searchable representation only when Room emits new entries.
    val index = remember(entries) {
        entries.map { entry ->
            IndexedArchiveEntry(
                entry,
                listOf(
                    entry.text,
                    entry.location,
                    ArchiveViewModel.weatherLabel(entry.weather),
                    ArchiveViewModel.moodLabel(entry.mood),
                    ArchiveFormatters.searchDate(entry.createdAt),
                ).joinToString(" ").lowercase(),
            )
        }
    }
    val terms = remember(settledQuery) {
        settledQuery.trim().lowercase().split(Regex("\\s+")).filter { it.isNotBlank() }
    }
    val results = remember(index, terms) {
        if (terms.isEmpty()) emptyList() else index.asSequence()
            .filter { indexed -> terms.all(indexed.haystack::contains) }
            .map { it.entry }
            .toList()
    }

    Column(Modifier.fillMaxSize().background(Color.White)) {
        ArchiveHeader(title = "搜索", onBack = onBack)
        Column(Modifier.fillMaxSize().padding(horizontal = 22.dp, vertical = 8.dp)) {
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(ArchiveColors.Surface).padding(horizontal = 13.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(ArchiveIcons.Search, null, tint = ArchiveColors.Tertiary, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(9.dp))
                BasicTextField(
                    value = query,
                    onValueChange = { query = it },
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 14.sp, color = ArchiveColors.Text),
                    modifier = Modifier.weight(1f),
                    decorationBox = { inner ->
                        Box {
                            if (query.isBlank()) Text("搜索文字、地点、心情或日期", color = ArchiveColors.Tertiary, fontSize = 14.sp)
                            inner()
                        }
                    },
                )
                if (query.isNotBlank()) {
                    Icon(ArchiveIcons.Close, null, tint = ArchiveColors.Tertiary, modifier = Modifier.size(16.dp).clickable { query = "" })
                }
            }
            Text(
                when {
                    query.isBlank() -> ""
                    settledQuery != query -> "正在搜索…"
                    results.isEmpty() -> "没有匹配的记录"
                    else -> "找到 ${results.size} 条记录"
                },
                color = ArchiveColors.Tertiary,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 8.dp, start = 2.dp, bottom = 4.dp),
            )
            if (query.isBlank()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("搜索你的 Archive\n支持文字、地点、心情、天气与日期", color = ArchiveColors.Tertiary, fontSize = 12.sp, lineHeight = 20.sp)
                }
            } else {
                LazyColumn(Modifier.fillMaxSize()) {
                    items(results, key = { it.id }, contentType = { "search-result" }) { entry -> SearchResult(entry, onEntry) }
                }
            }
        }
    }
}

@Composable
private fun SearchResult(entry: ArchiveEntry, onEntry: (String) -> Unit) {
    Column(Modifier.fillMaxWidth().clickable { onEntry(entry.id) }.padding(vertical = 15.dp)) {
        Text(ArchiveFormatters.resultDateTime(entry.createdAt), fontSize = 10.sp, color = ArchiveColors.Tertiary, modifier = Modifier.padding(bottom = 5.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(
                    entry.text.ifBlank { "仅照片记录" },
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
                val meta = listOfNotNull(
                    entry.location.takeIf { it.isNotBlank() },
                    ArchiveViewModel.moodLabel(entry.mood).takeIf { it.isNotBlank() },
                    ArchiveViewModel.weatherLabel(entry.weather).takeIf { it.isNotBlank() },
                )
                if (meta.isNotEmpty()) {
                    Text(meta.joinToString("   "), fontSize = 10.sp, color = ArchiveColors.Secondary, modifier = Modifier.padding(top = 7.dp))
                }
            }
            entry.images.firstOrNull()?.let { uri ->
                DemoOrRemoteImage(uri, Modifier.size(64.dp).clip(RoundedCornerShape(13.dp)), ContentScale.Crop)
            }
        }
        Box(Modifier.fillMaxWidth().padding(top = 15.dp).height(1.dp).background(Color(0xFFF1F1F1)))
    }
}
