package com.king0929zion.openarchive.ui.screens

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout

/** Bottom placement helper for sheet content emitted by a child composable outside BoxScope. */
fun Modifier.align(alignment: Alignment): Modifier {
    if (alignment != Alignment.BottomCenter) return this
    return this.layout { measurable, constraints ->
        val placeable = measurable.measure(constraints.copy(minWidth = 0, minHeight = 0))
        val width = constraints.maxWidth.coerceAtLeast(placeable.width)
        val height = constraints.maxHeight.coerceAtLeast(placeable.height)
        layout(width, height) {
            placeable.placeRelative((width - placeable.width) / 2, height - placeable.height)
        }
    }
}
