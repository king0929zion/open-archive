package com.king0929zion.openarchive.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.king0929zion.openarchive.ui.theme.ArchiveColors

fun diceBearUrl(seed: String): String =
    "https://api.dicebear.com/10.x/glyphs/svg?seed=" + java.net.URLEncoder.encode(seed, "UTF-8")

@Composable
fun ArchiveAvatar(seed: String, size: Int, modifier: Modifier = Modifier) {
    AsyncImage(
        model = diceBearUrl(seed),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(ArchiveColors.Surface),
    )
}

@Composable
fun ArchiveHeader(
    title: String? = null,
    onBack: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .background(ArchiveColors.Background)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Box(modifier = Modifier.width(76.dp), contentAlignment = Alignment.CenterStart) {
            if (onBack != null) {
                Row(
                    modifier = Modifier.clickable(onClick = onBack).padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, null, modifier = Modifier.size(20.dp))
                    Text("返回", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
        Text(title.orEmpty(), fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        Box(modifier = Modifier.width(76.dp), contentAlignment = Alignment.CenterEnd) {
            trailing?.invoke()
        }
    }
}

@Composable
fun DemoOrRemoteImage(
    uri: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
    if (uri.startsWith("demo://")) {
        val brush = when (uri.removePrefix("demo://")) {
            "cafe" -> Brush.linearGradient(listOf(Color(0xFFEFE6D8), Color(0xFFCAB59C), Color(0xFF8B6E55)))
            "box" -> Brush.linearGradient(listOf(Color(0xFFD9D4CC), Color(0xFFB4875D), Color(0xFF343434)))
            "book" -> Brush.linearGradient(listOf(Color(0xFFF6F0E4), Color(0xFFC8B8A5)))
            else -> Brush.linearGradient(listOf(Color(0xFF68716D), Color(0xFFF2EEE8), Color(0xFF292929)))
        }
        Box(modifier = modifier.background(brush))
    } else {
        AsyncImage(
            model = uri,
            contentDescription = null,
            contentScale = contentScale,
            modifier = modifier.background(ArchiveColors.Surface),
        )
    }
}

@Composable
fun ArchivePill(
    label: String,
    icon: @Composable (() -> Unit)? = null,
    selected: Boolean = false,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (selected) Color(0xFFF2F2F2) else Color(0xFFF6F6F6))
            .clickable(onClick = onClick)
            .padding(horizontal = 11.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        icon?.invoke()
        Text(
            text = label,
            color = if (selected) ArchiveColors.Text else ArchiveColors.Secondary,
            fontSize = 11.sp,
            maxLines = 1,
        )
    }
}

@Composable
fun SectionSpacer(height: Int = 16) = Spacer(Modifier.height(height.dp))
