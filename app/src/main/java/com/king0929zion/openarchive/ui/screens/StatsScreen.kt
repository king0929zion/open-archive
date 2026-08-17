package com.king0929zion.openarchive.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.king0929zion.openarchive.ArchiveViewModel
import com.king0929zion.openarchive.ui.components.ArchiveHeader
import com.king0929zion.openarchive.ui.theme.ArchiveColors
import java.util.Calendar

@Composable
fun StatsScreen(viewModel: ArchiveViewModel, onBack: () -> Unit) {
    val entries by viewModel.entries.collectAsStateWithLifecycle()
    val comments by viewModel.comments.collectAsStateWithLifecycle()
    val imageCount = entries.sumOf { it.images.size }
    val weekCounts = (6 downTo 0).map { offset ->
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_YEAR, -offset)
        }
        val start = cal.timeInMillis
        val end = start + 86_400_000L
        entries.count { it.createdAt in start until end }
    }
    val max = weekCounts.maxOrNull()?.coerceAtLeast(1) ?: 1
    val labels = listOf("日", "一", "二", "三", "四", "五", "六")
    val calendar = Calendar.getInstance()
    val dayLabels = (6 downTo 0).map { offset ->
        calendar.clone().let { it as Calendar }.apply { add(Calendar.DAY_OF_YEAR, -offset) }.let { labels[it.get(Calendar.DAY_OF_WEEK) - 1] }
    }

    Column(Modifier.fillMaxSize().background(Color.White)) {
        ArchiveHeader(title = "统计", onBack = onBack)
        Column(Modifier.fillMaxSize().padding(horizontal = 22.dp, vertical = 8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard(entries.size, "记录", Modifier.weight(1f))
                StatCard(imageCount, "照片", Modifier.weight(1f))
                StatCard(comments.size, "评论", Modifier.weight(1f))
            }
            Text("最近 7 天", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 22.dp, bottom = 12.dp))
            Row(
                Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(20.dp)).background(ArchiveColors.Surface).padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                weekCounts.forEachIndexed { index, count ->
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
                        Spacer(Modifier.weight(1f))
                        Box(
                            Modifier.widthIn(max = 20.dp).fillMaxWidth(0.65f)
                                .height(if (count == 0) 4.dp else (18 + (count.toFloat() / max * 70)).dp)
                                .clip(RoundedCornerShape(7.dp))
                                .background(if (count == 0) Color(0xFFE8E8E8) else ArchiveColors.Dark)
                        )
                        Text(dayLabels[index], fontSize = 10.sp, color = ArchiveColors.Tertiary, modifier = Modifier.padding(top = 6.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(value: Int, label: String, modifier: Modifier) {
    Column(
        modifier.clip(RoundedCornerShape(20.dp)).background(ArchiveColors.Surface).padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(value.toString(), fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        Text(label, fontSize = 11.sp, color = ArchiveColors.Secondary, modifier = Modifier.padding(top = 2.dp))
    }
}
