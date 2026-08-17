package com.king0929zion.openarchive.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object ArchiveColors {
    val Background = Color(0xFFFFFFFF)
    val Text = Color(0xFF111111)
    val Secondary = Color(0xFF888888)
    val Tertiary = Color(0xFFBBBBBB)
    val Surface = Color(0xFFF7F7F7)
    val SurfacePressed = Color(0xFFF1F1F1)
    val Dark = Color(0xFF222222)
    val Danger = Color(0xFFE5484D)
}

private val scheme = lightColorScheme(
    primary = ArchiveColors.Dark,
    onPrimary = Color.White,
    background = ArchiveColors.Background,
    onBackground = ArchiveColors.Text,
    surface = ArchiveColors.Background,
    onSurface = ArchiveColors.Text,
    error = ArchiveColors.Danger,
)

private val type = Typography(
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 22.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
    ),
)

@Composable
fun OpenArchiveTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = scheme, typography = type, content = content)
}
