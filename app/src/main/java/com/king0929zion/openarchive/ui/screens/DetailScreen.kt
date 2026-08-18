package com.king0929zion.openarchive.ui.screens

import android.content.Intent
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.king0929zion.openarchive.ArchiveViewModel
import com.king0929zion.openarchive.data.ArchiveComment
import com.king0929zion.openarchive.ui.ArchiveFormatters
import com.king0929zion.openarchive.ui.components.ArchiveAvatar
import com.king0929zion.openarchive.ui.components.ArchiveHeader
import com.king0929zion.openarchive.ui.components.EntryImageGrid
import com.king0929zion.openarchive.ui.icons.ArchiveIcons
import com.king0929zion.openarchive.ui.theme.ArchiveColors

@Composable
fun DetailScreen(viewModel: ArchiveViewModel, entryId: String, onBack: () -> Unit) {
    val entries by viewModel.entries.collectAsStateWithLifecycle()
    val allComments by viewModel.comments.collectAsStateWithLifecycle()
    val avatarSeed by viewModel.avatarSeed.collectAsStateWithLifecycle()
    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val entry = remember(entries, entryId) { entries.firstOrNull { it.id == entryId } }
    val context = LocalContext.current
    var menuOpen by remember { mutableStateOf(false) }
    var deleteConfirm by remember { mutableStateOf(false) }
    var commentText by remember { mutableStateOf("") }
    var replyTo by remember { mutableStateOf<ArchiveComment?>(null) }

    if (entry == null) {
        Column(Modifier.fillMaxSize().background(Color.White)) {
            ArchiveHeader(onBack = onBack)
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("记录不存在", color = ArchiveColors.Tertiary) }
        }
        return
    }

    val comments = remember(allComments, entryId) { allComments.filter { it.entryId == entryId } }
    val commentsByParent = remember(comments) { comments.groupBy { it.parentId } }
    val top = commentsByParent[null].orEmpty()
    val date = ArchiveFormatters.detailDate(entry.createdAt)
    val time = ArchiveFormatters.detailTime(entry.createdAt)

    Column(Modifier.fillMaxSize().background(Color.White)) {
        ArchiveHeader(onBack = onBack)
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 22.dp, end = 22.dp, bottom = 24.dp),
        ) {
            item(key = "entry-$entryId", contentType = "detail-entry") {
                Row(Modifier.padding(top = 8.dp, bottom = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    ArchiveAvatar(avatarSeed, 38)
                    Spacer(Modifier.width(10.dp)); Text(userName, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
                if (entry.text.isNotBlank()) Text(entry.text, fontSize = 14.sp, lineHeight = 24.sp, modifier = Modifier.padding(bottom = 14.dp))
                EntryImageGrid(entry.images, detail = true)
                Column(Modifier.fillMaxWidth().padding(top = 18.dp)) {
                    Text("$date  ·  $time", fontSize = 11.sp, color = ArchiveColors.Secondary)
                    Row(
                        Modifier.fillMaxWidth().height(32.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            if (entry.location.isNotBlank()) MetaText(ArchiveIcons.Location, entry.location)
                            ArchiveViewModel.weatherLabel(entry.weather).takeIf { it.isNotBlank() }?.let { MetaText(detailWeatherIcon(entry.weather), it) }
                            ArchiveViewModel.moodLabel(entry.mood).takeIf { it.isNotBlank() }?.let { MetaText(detailMoodIcon(entry.mood), it) }
                            if (entry.location.isBlank() && entry.weather.isBlank() && entry.mood.isBlank()) {
                                Text("未添加位置或状态", fontSize = 10.sp, color = ArchiveColors.Tertiary)
                            }
                        }
                        Box {
                            Box(
                                Modifier.size(28.dp).clip(CircleShape).clickable { menuOpen = !menuOpen },
                                contentAlignment = Alignment.Center,
                            ) { Icon(ArchiveIcons.More, "更多操作", tint = ArchiveColors.Secondary, modifier = Modifier.size(18.dp)) }
                            if (menuOpen) {
                                Popup(
                                    alignment = Alignment.TopEnd,
                                    offset = IntOffset(0, 30),
                                    onDismissRequest = { menuOpen = false },
                                    properties = PopupProperties(focusable = true),
                                ) {
                                    Column(
                                        Modifier.width(132.dp)
                                            .shadow(4.dp, RoundedCornerShape(16.dp), ambientColor = Color.Black.copy(.025f), spotColor = Color.Black.copy(.025f))
                                            .background(Color.White, RoundedCornerShape(16.dp)).padding(6.dp)
                                    ) {
                                        ActionRow("分享", false, ArchiveIcons.Share) {
                                            menuOpen = false
                                            val meta = listOfNotNull(
                                                "$date $time",
                                                entry.location.takeIf { it.isNotBlank() },
                                                ArchiveViewModel.weatherLabel(entry.weather).takeIf { it.isNotBlank() },
                                                ArchiveViewModel.moodLabel(entry.mood).takeIf { it.isNotBlank() },
                                            ).joinToString(" · ")
                                            val text = listOf(entry.text, meta).filter { it.isNotBlank() }.joinToString("\n\n")
                                            context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                                                type = "text/plain"; putExtra(Intent.EXTRA_TEXT, text)
                                            }, null))
                                        }
                                        ActionRow("删除", true, ArchiveIcons.Delete) { menuOpen = false; deleteConfirm = true }
                                    }
                                }
                            }
                        }
                    }
                    Text("评论  ${comments.size}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 24.dp, bottom = 14.dp))
                    if (top.isEmpty()) Text("还没有评论，来说点什么吧", color = ArchiveColors.Tertiary, fontSize = 12.sp)
                }
            }
            items(top, key = { it.id }, contentType = { "comment" }) { comment ->
                CommentBlock(comment, commentsByParent[comment.id].orEmpty(), onReply = { replyTo = comment })
            }
        }

        if (replyTo != null) {
            Row(
                Modifier.fillMaxWidth().background(ArchiveColors.Surface).padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("回复 @${replyTo?.authorName}", fontSize = 11.sp, color = ArchiveColors.Secondary)
                Icon(ArchiveIcons.Close, "取消回复", tint = ArchiveColors.Tertiary, modifier = Modifier.size(18.dp).clickable { replyTo = null }.padding(2.dp))
            }
        }
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            ArchiveAvatar(avatarSeed, 28)
            BasicTextField(
                value = commentText,
                onValueChange = { commentText = it },
                singleLine = true,
                textStyle = TextStyle(fontSize = 13.sp, color = ArchiveColors.Text),
                modifier = Modifier.weight(1f).clip(RoundedCornerShape(18.dp)).background(ArchiveColors.Surface).padding(horizontal = 14.dp, vertical = 9.dp),
                decorationBox = { inner -> Box { if (commentText.isBlank()) Text(replyTo?.let { "回复 @${it.authorName}" } ?: "写下评论…", color = ArchiveColors.Tertiary, fontSize = 13.sp); inner() } },
            )
            Box(
                Modifier.size(34.dp).clip(CircleShape).background(if (commentText.isBlank()) ArchiveColors.Surface else ArchiveColors.Dark)
                    .clickable(enabled = commentText.isNotBlank()) {
                        viewModel.addComment(entryId, commentText, replyTo?.id); commentText = ""; replyTo = null
                    },
                contentAlignment = Alignment.Center,
            ) { Icon(ArchiveIcons.Send, null, tint = if (commentText.isBlank()) ArchiveColors.Tertiary else Color.White, modifier = Modifier.size(15.dp)) }
        }
    }

    if (deleteConfirm) {
        AlertDialog(
            onDismissRequest = { deleteConfirm = false },
            title = { Text("删除这条记录？") },
            text = { Text("此操作无法撤销。") },
            confirmButton = { Text("删除", color = ArchiveColors.Danger, modifier = Modifier.clickable { viewModel.deleteEntry(entryId) { onBack() } }.padding(12.dp)) },
            dismissButton = { Text("取消", modifier = Modifier.clickable { deleteConfirm = false }.padding(12.dp)) },
            containerColor = Color.White,
        )
    }
}

@Composable
private fun MetaText(icon: ImageVector, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(icon, null, tint = ArchiveColors.Secondary, modifier = Modifier.size(12.dp))
        Text(label, color = ArchiveColors.Secondary, fontSize = 11.sp, maxLines = 1)
    }
}

@Composable
private fun ActionRow(label: String, danger: Boolean, icon: ImageVector, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(11.dp)).clickable(onClick = onClick).padding(horizontal = 11.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        Icon(icon, null, tint = if (danger) ArchiveColors.Danger else ArchiveColors.Text, modifier = Modifier.size(15.dp))
        Text(label, fontSize = 13.sp, color = if (danger) ArchiveColors.Danger else ArchiveColors.Text)
    }
}

@Composable
private fun CommentBlock(comment: ArchiveComment, replies: List<ArchiveComment>, onReply: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(bottom = 16.dp), verticalAlignment = Alignment.Top) {
        ArchiveAvatar(comment.avatarSeed, 28)
        Spacer(Modifier.width(9.dp))
        Column(Modifier.weight(1f)) {
            Text(comment.authorName, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = ArchiveColors.Secondary)
            Text(comment.text, fontSize = 13.sp, lineHeight = 20.sp)
            replies.forEach { reply ->
                Row(Modifier.padding(top = 9.dp), verticalAlignment = Alignment.Top) {
                    ArchiveAvatar(reply.avatarSeed, 20)
                    Spacer(Modifier.width(7.dp))
                    Text("${reply.authorName}：${reply.text}", fontSize = 12.sp, lineHeight = 18.sp)
                }
            }
        }
        Icon(ArchiveIcons.Reply, null, tint = ArchiveColors.Tertiary, modifier = Modifier.size(26.dp).padding(6.dp).clickable(onClick = onReply))
    }
}

private fun detailWeatherIcon(key: String) = when (key) {
    "sunny" -> ArchiveIcons.Sun
    "overcast" -> ArchiveIcons.Cloud
    "rain" -> ArchiveIcons.CloudRain
    "snow" -> ArchiveIcons.Snowflake
    else -> ArchiveIcons.CloudSun
}

private fun detailMoodIcon(key: String) = when (key) {
    "low" -> ArchiveIcons.Moon
    "calm" -> ArchiveIcons.Leaf
    "cozy" -> ArchiveIcons.Coffee
    "energy" -> ArchiveIcons.Zap
    else -> ArchiveIcons.Smile
}
