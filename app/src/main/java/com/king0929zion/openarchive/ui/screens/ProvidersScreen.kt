package com.king0929zion.openarchive.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.king0929zion.openarchive.ArchiveViewModel
import com.king0929zion.openarchive.ui.components.ArchiveHeader
import com.king0929zion.openarchive.ui.icons.ArchiveIcons
import com.king0929zion.openarchive.ui.theme.ArchiveColors

@Composable
fun ProvidersScreen(viewModel: ArchiveViewModel, onBack: () -> Unit, onEdit: (String?) -> Unit) {
    val providers by viewModel.providers.collectAsStateWithLifecycle()
    val defaultProviderId by viewModel.defaultProviderId.collectAsStateWithLifecycle()
    val defaultModelId by viewModel.defaultModelId.collectAsStateWithLifecycle()
    var pickerOpen by remember { mutableStateOf(false) }
    val defaultProvider = providers.firstOrNull { it.id == defaultProviderId }
    val defaultModel = defaultProvider?.models?.firstOrNull { it.modelId == defaultModelId }
    BackHandler(pickerOpen) { pickerOpen = false }

    Box(Modifier.fillMaxSize().background(Color.White)) {
        Column(Modifier.fillMaxSize()) {
            ArchiveHeader(title = "AI 模型", onBack = onBack)
            Column(Modifier.fillMaxSize().padding(horizontal = 22.dp, vertical = 4.dp)) {
                Text("默认模型", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 12.dp, bottom = 8.dp))
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(ArchiveColors.Surface)
                        .clickable { if (providers.any { it.models.isNotEmpty() }) pickerOpen = true }
                        .padding(horizontal = 15.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(defaultModel?.displayName?.ifBlank { defaultModel.modelId } ?: "未设置默认模型", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                        Text(defaultProvider?.name ?: "选择 Achi 默认使用的模型", fontSize = 9.5.sp, color = ArchiveColors.Tertiary)
                    }
                    Icon(ArchiveIcons.ChevronRight, null, tint = ArchiveColors.Tertiary, modifier = Modifier.size(16.dp))
                }
                Row(Modifier.fillMaxWidth().padding(top = 18.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("供应商", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    Text("添加", fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable { onEdit(null) }.padding(6.dp))
                }
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(ArchiveColors.Surface)) {
                    if (providers.isEmpty()) Text("还没有供应商", color = ArchiveColors.Tertiary, fontSize = 10.5.sp, modifier = Modifier.align(Alignment.CenterHorizontally).padding(22.dp))
                    providers.forEachIndexed { index, provider ->
                        Row(Modifier.fillMaxWidth().clickable { onEdit(provider.id) }.padding(horizontal = 15.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(provider.name, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    if (provider.id == defaultProviderId) {
                                        Spacer(Modifier.width(7.dp)); Text("默认", fontSize = 8.5.sp, color = ArchiveColors.Secondary, modifier = Modifier.clip(CircleShape).background(Color(0xFFECECEC)).padding(horizontal = 5.dp, vertical = 1.dp))
                                    }
                                }
                                Text("${formatLabel(provider.format)} · ${provider.models.size} 个模型", fontSize = 9.5.sp, color = ArchiveColors.Tertiary)
                            }
                            Icon(ArchiveIcons.ChevronRight, null, tint = ArchiveColors.Tertiary, modifier = Modifier.size(16.dp))
                        }
                        if (index != providers.lastIndex) Box(Modifier.fillMaxWidth().padding(start = 15.dp).height(1.dp).background(Color.Black.copy(alpha = .035f)))
                    }
                }
            }
        }
        if (pickerOpen) {
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = .20f)).clickable { pickerOpen = false })
            Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth().clip(RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp)).background(Color.White).padding(bottom = 22.dp)) {
                Box(Modifier.fillMaxWidth().padding(top = 9.dp), contentAlignment = Alignment.Center) { Box(Modifier.width(32.dp).height(3.dp).clip(CircleShape).background(Color(0xFFE8E8E8))) }
                Text("默认模型", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(18.dp, 12.dp, 18.dp, 8.dp))
                providers.filter { it.models.isNotEmpty() }.forEach { provider ->
                    Text(provider.name, fontSize = 9.5.sp, color = ArchiveColors.Tertiary, modifier = Modifier.padding(start = 20.dp, top = 8.dp, bottom = 3.dp))
                    provider.models.forEach { model ->
                        val selected = provider.id == defaultProviderId && model.modelId == defaultModelId
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = 12.dp).clip(RoundedCornerShape(12.dp))
                                .background(if (selected) Color(0xFFF4F4F4) else Color.Transparent)
                                .clickable { viewModel.setDefaultModel(provider.id, model.modelId); pickerOpen = false }
                                .padding(horizontal = 10.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(model.displayName.ifBlank { model.modelId }, fontSize = 11.5.sp, fontWeight = FontWeight.Medium)
                                if (model.displayName.isNotBlank()) Text(model.modelId, fontSize = 9.5.sp, color = ArchiveColors.Tertiary)
                            }
                            if (selected) Icon(ArchiveIcons.Check, null, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }
    }
}

fun formatLabel(format: String): String = when (format.lowercase()) { "anthropic" -> "Anthropic"; "responses" -> "Responses"; else -> "OpenAI" }
