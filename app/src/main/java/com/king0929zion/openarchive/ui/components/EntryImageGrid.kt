package com.king0929zion.openarchive.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
    val gap = 3.dp
    val visibleRows = remember(images) { images.take(9).chunked(3) }

    Column(
        modifier = modifier.fillMaxWidth().clip(RoundedCornerShape(radius)),
        verticalArrangement = Arrangement.spacedBy(gap),
    ) {
        when (images.size) {
            1 -> DemoOrRemoteImage(
                images[0],
                Modifier.fillMaxWidth().aspectRatio(4f / 3f),
            )

            2 -> Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(gap),
            ) {
                images.take(2).forEach { uri ->
                    DemoOrRemoteImage(uri, Modifier.weight(1f).aspectRatio(1f))
                }
            }

            3 -> Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(gap),
            ) {
                images.take(3).forEach { uri ->
                    DemoOrRemoteImage(uri, Modifier.weight(1f).aspectRatio(1f))
                }
            }

            4 -> {
                images.take(4).chunked(2).forEach { row ->
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(gap),
                    ) {
                        row.forEach { uri ->
                            DemoOrRemoteImage(uri, Modifier.weight(1f).aspectRatio(1f))
                        }
                    }
                }
            }

            else -> visibleRows.forEach { row ->
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(gap),
                ) {
                    row.forEach { uri ->
                        DemoOrRemoteImage(uri, Modifier.weight(1f).aspectRatio(1f))
                    }
                    repeat(3 - row.size) {
                        Box(Modifier.weight(1f).aspectRatio(1f))
                    }
                }
            }
        }
    }
}
