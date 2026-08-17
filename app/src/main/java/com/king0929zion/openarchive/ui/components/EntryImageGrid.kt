package com.king0929zion.openarchive.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun EntryImageGrid(
    images: List<String>,
    modifier: Modifier = Modifier,
    detail: Boolean = false,
) {
    if (images.isEmpty()) return
    val radius = if (detail) 22.dp else 20.dp
    BoxWithConstraints(modifier = modifier.fillMaxWidth().clip(RoundedCornerShape(radius))) {
        val gap = 3.dp
        val width = maxWidth
        when (images.size) {
            1 -> DemoOrRemoteImage(
                images[0],
                Modifier.fillMaxWidth().aspectRatio(4f / 3f).clip(RoundedCornerShape(radius)),
            )
            2 -> Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                val w = (width - gap) / 2
                images.take(2).forEach { DemoOrRemoteImage(it, Modifier.width(w).height(w)) }
            }
            3 -> Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                val w = (width - gap * 2) / 3
                images.take(3).forEach { DemoOrRemoteImage(it, Modifier.size(w)) }
            }
            4 -> Column(verticalArrangement = Arrangement.spacedBy(gap)) {
                val w = (width - gap) / 2
                images.chunked(2).take(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                        row.forEach { DemoOrRemoteImage(it, Modifier.size(w)) }
                    }
                }
            }
            else -> Column(verticalArrangement = Arrangement.spacedBy(gap)) {
                val w = (width - gap * 2) / 3
                images.take(9).chunked(3).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                        row.forEach { DemoOrRemoteImage(it, Modifier.size(w)) }
                        repeat(3 - row.size) { Box(Modifier.size(w)) }
                    }
                }
            }
        }
    }
}
