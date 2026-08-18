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
            1 -> DemoOrRemoteImage(images[0], Modifier.fillMaxWidth().aspectRatio(4f / 3f))
            2 -> Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                val w = (width - gap) / 2
                DemoOrRemoteImage(images[0], Modifier.width(w).height(w))
                DemoOrRemoteImage(images[1], Modifier.width(w).height(w))
            }
            3 -> Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                val w = (width - gap * 2) / 3
                repeat(3) { index -> DemoOrRemoteImage(images[index], Modifier.size(w)) }
            }
            4 -> Column(verticalArrangement = Arrangement.spacedBy(gap)) {
                val w = (width - gap) / 2
                Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                    DemoOrRemoteImage(images[0], Modifier.size(w)); DemoOrRemoteImage(images[1], Modifier.size(w))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                    DemoOrRemoteImage(images[2], Modifier.size(w)); DemoOrRemoteImage(images[3], Modifier.size(w))
                }
            }
            else -> Column(verticalArrangement = Arrangement.spacedBy(gap)) {
                val w = (width - gap * 2) / 3
                val count = minOf(images.size, 9)
                var index = 0
                while (index < count) {
                    Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                        repeat(3) { column ->
                            val imageIndex = index + column
                            if (imageIndex < count) DemoOrRemoteImage(images[imageIndex], Modifier.size(w)) else Box(Modifier.size(w))
                        }
                    }
                    index += 3
                }
            }
        }
    }
}
