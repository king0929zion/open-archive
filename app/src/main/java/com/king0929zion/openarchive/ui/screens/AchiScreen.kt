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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.Stop
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.king0929zion.openarchive.ArchiveViewModel
import com.king0929zion.openarchive.ui.components.ArchiveHeader
import com.king0929zion.openarchive.ui.theme.ArchiveColors

@Composable
fun AchiScreen(viewModel: ArchiveViewModel, onBack: () -> Unit, onSettings: () -> Unit) {
    val messages by viewModel.achiMessages.collectAsStateWithLifecycle()
    val streaming by viewModel.achiStreaming.collectAsStateWithLifecycle()
    val providers by viewModel.providers.collectAsStateWithLifecycle()
    val defaultProvider by viewModel.defaultProviderId.collectAsStateWithLifecycle()
    val defaultModel by viewModel.defaultModelId.collectAsStateWithLifecycle()
    val configured = providers.any { p -> p.id == defaultProvider && p.models.any { it.modelId == defaultModel } }
    var input by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    LaunchedEffect(messages.size, messages.lastOrNull()?.text) { if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex) }

    Column(Modifier.fillMaxSize().background(Color.White).imePadding()) {
        ArchiveHeader(title = "Achi", onBack = onBack)
        LazyColumn(state = listState, modifier = Modifier.weight(1f).fillMaxWidth(), contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 22.dp, vertical = 8.dp)) {
            if (!configured) item {
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(ArchiveColors.Surface).padding(16.dp)) {
                    Text("还没有设置 AI 模型", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Text("配置供应商并选择默认模型后，Achi 才会真正发送请求。", fontSize = 11.sp, lineHeight = 18.sp, color = ArchiveColors.Secondary, modifier = Modifier.padding(top = 4.dp))
                    Text("去设置", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 10.dp).clip(CircleShape).background(Color.White).clickable(onClick = onSettings).padding(horizontal = 12.dp, vertical = 7.dp))
                }
            }
            items(messages, key = { it.id }) { message ->
                Row(Modifier.fillMaxWidth().padding(vertical = 7.dp), horizontalArrangement = if (message.fromUser) Arrangement.End else Arrangement.Start, verticalAlignment = Alignment.Top) {
                    if (!message.fromUser) {
                        Box(Modifier.size(28.dp).clip(CircleShape).background(ArchiveColors.Surface), contentAlignment = Alignment.Center) { Icon(Icons.Rounded.AutoAwesome, null, modifier = Modifier.size(14.dp), tint = ArchiveColors.Text) }
                        Spacer(Modifier.width(8.dp))
                    }
                    Box(Modifier.fillMaxWidth(0.76f).clip(RoundedCornerShape(topStart = if (message.fromUser) 18.dp else 6.dp, topEnd = if (message.fromUser) 6.dp else 18.dp, bottomStart = 18.dp, bottomEnd = 18.dp)).background(if (message.fromUser) ArchiveColors.Dark else ArchiveColors.Surface).padding(horizontal = 14.dp, vertical = 10.dp)) {
                        Text(if (!message.fromUser && message.text.isBlank() && streaming) "…" else message.text, color = if (message.fromUser) Color.White else ArchiveColors.Text, fontSize = 13.sp, lineHeight = 20.sp)
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            BasicTextField(value = input, onValueChange = { input = it }, singleLine = true, textStyle = TextStyle(fontSize = 13.sp, color = ArchiveColors.Text), modifier = Modifier.weight(1f).clip(RoundedCornerShape(18.dp)).background(ArchiveColors.Surface).padding(horizontal = 14.dp, vertical = 9.dp), decorationBox = { inner -> Box { if (input.isBlank()) Text("和 Achi 聊聊…", color = ArchiveColors.Tertiary, fontSize = 13.sp); inner() } })
            Box(Modifier.size(34.dp).clip(CircleShape).background(if (streaming || input.isNotBlank()) ArchiveColors.Dark else ArchiveColors.Surface).clickable(enabled = streaming || input.isNotBlank()) { if (streaming) viewModel.stopAchi() else { viewModel.sendAchi(input); input = "" } }, contentAlignment = Alignment.Center) {
                Icon(if (streaming) Icons.Rounded.Stop else Icons.Rounded.Send, null, tint = if (streaming || input.isNotBlank()) Color.White else ArchiveColors.Tertiary, modifier = Modifier.size(15.dp))
            }
        }
    }
}
