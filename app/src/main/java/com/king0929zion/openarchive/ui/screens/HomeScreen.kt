package com.king0929zion.openarchive.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.king0929zion.openarchive.ArchiveViewModel
import com.king0929zion.openarchive.data.ArchiveEntry
import com.king0929zion.openarchive.ui.ArchiveFormatters
import com.king0929zion.openarchive.ui.ArchiveMotion
import com.king0929zion.openarchive.ui.components.ArchiveAvatar
import com.king0929zion.openarchive.ui.components.EntryImageGrid
import com.king0929zion.openarchive.ui.icons.ArchiveIcons
import com.king0929zion.openarchive.ui.theme.ArchiveColors

@Composable
fun HomeScreen(
    viewModel: ArchiveViewModel,
    onCompose: () -> Unit,
    onEntry: (String) -> Unit,
    onNavigate: (String) -> Unit,
) {
    val entries by viewModel.entries.collectAsStateWithLifecycle()
    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val avatarSeed by viewModel.avatarSeed.collectAsStateWithLifecycle()
    var drawerOpen by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize().background(ArchiveColors.Background)) {
        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier.fillMaxWidth().height(58.dp).padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    Modifier.clickable { drawerOpen = true },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(9.dp),
                ) {
                    ArchiveAvatar(avatarSeed, 32)
                    Text(userName, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
                Box(
                    Modifier.size(34.dp).clip(CircleShape).background(ArchiveColors.Dark).clickable(onClick = onCompose),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(ArchiveIcons.Add, null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }

            if (entries.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("还没有记录\n点右上角 + 留下第一条 Archive", color = ArchiveColors.Tertiary, fontSize = 12.sp, lineHeight = 20.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 10.dp, end = 20.dp, bottom = 80.dp),
                ) {
                    items(entries, key = { it.id }, contentType = { "archive-entry" }) { entry ->
                        FeedEntry(entry, onClick = { onEntry(entry.id) })
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = drawerOpen,
            enter = fadeIn(
                tween(
                    durationMillis = ArchiveMotion.Fast,
                    easing = ArchiveMotion.Easing,
                )
            ),
            exit = fadeOut(
                tween(
                    durationMillis = ArchiveMotion.Quick,
                    easing = ArchiveMotion.Easing,
                )
            ),
        ) {
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.28f)).clickable { drawerOpen = false })
        }
        AnimatedVisibility(
            visible = drawerOpen,
            enter = slideInHorizontally(
                animationSpec = tween(
                    durationMillis = ArchiveMotion.Standard,
                    easing = ArchiveMotion.Easing,
                ),
                initialOffsetX = { -it },
            ) + fadeIn(
                tween(
                    durationMillis = ArchiveMotion.Fast,
                    easing = ArchiveMotion.Easing,
                )
            ),
            exit = slideOutHorizontally(
                animationSpec = tween(
                    durationMillis = ArchiveMotion.Fast,
                    easing = ArchiveMotion.Easing,
                ),
                targetOffsetX = { -it },
            ) + fadeOut(
                tween(
                    durationMillis = ArchiveMotion.Quick,
                    easing = ArchiveMotion.Easing,
                )
            ),
        ) {
            ArchiveDrawer(
                userName = userName,
                avatarSeed = avatarSeed,
                onNavigate = { drawerOpen = false; onNavigate(it) },
            )
        }
    }
}

@Composable
private fun FeedEntry(entry: ArchiveEntry, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(top = 34.dp).clickable(onClick = onClick),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Row(Modifier.width(46.dp).padding(top = 2.dp), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(ArchiveFormatters.feedDay(entry.createdAt), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(ArchiveFormatters.feedMonth(entry.createdAt), fontSize = 9.sp, fontWeight = FontWeight.Medium)
        }
        Column(Modifier.weight(1f)) {
            if (entry.text.isNotBlank()) {
                Text(
                    text = entry.text,
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    maxLines = 5,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(bottom = if (entry.images.isNotEmpty()) 10.dp else 0.dp),
                )
            }
            EntryImageGrid(entry.images)
        }
    }
}

@Composable
private fun ArchiveDrawer(
    userName: String,
    avatarSeed: String,
    onNavigate: (String) -> Unit,
) {
    Column(
        Modifier.fillMaxHeight().width(310.dp)
            .clip(RoundedCornerShape(topEnd = 28.dp, bottomEnd = 28.dp))
            .background(Color.White)
            .padding(top = 30.dp, bottom = 18.dp),
    ) {
        Row(Modifier.padding(horizontal = 22.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            ArchiveAvatar(avatarSeed, 48)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(userName, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Text("Record · Reflect · Remember", fontSize = 10.sp, color = ArchiveColors.Tertiary)
            }
        }
        Spacer(Modifier.height(18.dp))
        DrawerRow("Achi", ArchiveIcons.Achi) { onNavigate("achi") }
        DrawerRow("搜索", ArchiveIcons.Search) { onNavigate("search") }
        DrawerRow("相册", ArchiveIcons.Album) { onNavigate("album") }
        DrawerRow("统计", ArchiveIcons.Stats) { onNavigate("stats") }
        Spacer(Modifier.weight(1f))
        Box(Modifier.fillMaxWidth().height(1.dp).background(ArchiveColors.Surface))
        DrawerRow("设置", ArchiveIcons.Settings) { onNavigate("settings") }
    }
}

@Composable
private fun DrawerRow(label: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 22.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = ArchiveColors.Secondary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(13.dp))
        Text(label, Modifier.weight(1f), fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Icon(ArchiveIcons.ChevronRight, null, tint = ArchiveColors.Tertiary, modifier = Modifier.size(16.dp))
    }
}
