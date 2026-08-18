package com.king0929zion.openarchive.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.king0929zion.openarchive.ArchiveViewModel
import com.king0929zion.openarchive.ui.components.ArchiveAvatar
import com.king0929zion.openarchive.ui.components.ArchiveHeader
import com.king0929zion.openarchive.ui.icons.ArchiveIcons
import com.king0929zion.openarchive.ui.theme.ArchiveColors

@Composable
fun SettingsScreen(viewModel: ArchiveViewModel, onBack: () -> Unit, onProviders: () -> Unit) {
    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val avatarSeed by viewModel.avatarSeed.collectAsStateWithLifecycle()
    val providers by viewModel.providers.collectAsStateWithLifecycle()
    val defaultProvider by viewModel.defaultProviderId.collectAsStateWithLifecycle()
    val defaultModel by viewModel.defaultModelId.collectAsStateWithLifecycle()
    val provider = providers.firstOrNull { it.id == defaultProvider }
    val model = provider?.models?.firstOrNull { it.modelId == defaultModel }
    val summary = if (provider != null && model != null) "${model.displayName.ifBlank { model.modelId }} · ${provider.name}" else if (providers.isNotEmpty()) "${providers.size} 个供应商" else "未配置"

    Column(Modifier.fillMaxSize().background(Color.White)) {
        ArchiveHeader(title = "设置", onBack = onBack)
        Column(Modifier.fillMaxSize().padding(horizontal = 22.dp, vertical = 8.dp)) {
            Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                ArchiveAvatar(avatarSeed, 46)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(userName, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    Text("Open Archive", fontSize = 11.sp, color = ArchiveColors.Tertiary)
                }
            }
            Spacer(Modifier.size(12.dp))
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(ArchiveColors.Surface)
                    .clickable(onClick = onProviders).padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.size(32.dp).clip(RoundedCornerShape(11.dp)).background(Color.White), contentAlignment = Alignment.Center) {
                    Icon(ArchiveIcons.Server, null, modifier = Modifier.size(15.dp))
                }
                Spacer(Modifier.width(12.dp))
                Text("模型与供应商", fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                Text(summary, fontSize = 10.5.sp, color = ArchiveColors.Tertiary, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(.8f))
                Icon(ArchiveIcons.ChevronRight, null, tint = ArchiveColors.Tertiary, modifier = Modifier.size(16.dp))
            }
        }
    }
}
