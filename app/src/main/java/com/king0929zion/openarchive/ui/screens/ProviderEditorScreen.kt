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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.king0929zion.openarchive.ArchiveViewModel
import com.king0929zion.openarchive.data.ProviderModel
import com.king0929zion.openarchive.ui.components.ArchiveHeader
import com.king0929zion.openarchive.ui.theme.ArchiveColors

@Composable
fun ProviderEditorScreen(viewModel: ArchiveViewModel, providerId: String?, onBack: () -> Unit, onSaved: () -> Unit) {
    val providers by viewModel.providers.collectAsStateWithLifecycle()
    var initialized by remember(providerId) { mutableStateOf(false) }
    var name by remember(providerId) { mutableStateOf("") }
    var format by remember(providerId) { mutableStateOf("openai") }
    var baseUrl by remember(providerId) { mutableStateOf("") }
    var apiKey by remember(providerId) { mutableStateOf("") }
    var encryptedKey by remember(providerId) { mutableStateOf("") }
    var showKey by remember { mutableStateOf(false) }
    var customModel by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }
    var fetched by remember { mutableStateOf<List<String>>(emptyList()) }
    val models = remember(providerId) { mutableStateListOf<ProviderModel>() }

    LaunchedEffect(providerId, providers) {
        if (!initialized) {
            val provider = providers.firstOrNull { it.id == providerId }
            if (provider != null) {
                name = provider.name; format = provider.format; baseUrl = provider.baseUrl; encryptedKey = provider.encryptedApiKey
                models.clear(); models.addAll(provider.models)
            } else if (providerId == null) {
                baseUrl = "https://api.openai.com/v1"
            }
            if (provider != null || providerId == null) initialized = true
        }
    }

    Column(Modifier.fillMaxSize().background(Color.White)) {
        ArchiveHeader(
            title = if (providerId == null) "新供应商" else name.ifBlank { "供应商" },
            onBack = onBack,
            trailing = {
                Text("保存", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable(enabled = name.isNotBlank() && baseUrl.isNotBlank()) {
                    viewModel.saveProvider(providerId, name, format, baseUrl, apiKey, encryptedKey, models.toList(), onSaved)
                }.padding(8.dp))
            },
        )
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 22.dp, vertical = 8.dp)) {
            FieldLabel("名称"); PlainInput(name, { name = it }, "例如 Anthropic 官方")
            FieldLabel("格式", top = 15)
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(ArchiveColors.Surface).padding(3.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf("openai" to "OpenAI", "responses" to "Responses", "anthropic" to "Anthropic").forEach { (key, label) ->
                    Box(Modifier.weight(1f).clip(RoundedCornerShape(9.dp)).background(if (format == key) ArchiveColors.Dark else Color.Transparent).clickable { format = key }.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                        Text(label, fontSize = 10.5.sp, color = if (format == key) Color.White else ArchiveColors.Secondary)
                    }
                }
            }
            FieldLabel("Base URL", top = 15); PlainInput(baseUrl, { baseUrl = it }, if (format == "anthropic") "https://api.anthropic.com/v1" else "https://api.openai.com/v1")
            FieldLabel("API Key", top = 15)
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                PlainInput(apiKey, { apiKey = it }, if (encryptedKey.isNotBlank()) "已保存，留空则不修改" else "sk-…", Modifier.weight(1f), password = !showKey)
                Box(Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(ArchiveColors.Surface).clickable { showKey = !showKey }, contentAlignment = Alignment.Center) {
                    Icon(if (showKey) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility, null, tint = ArchiveColors.Secondary, modifier = Modifier.size(15.dp))
                }
            }
            Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                SmallAction("测试连接", Modifier.weight(1f)) {
                    status = "正在测试…"
                    viewModel.fetchModels(format, baseUrl, apiKey, encryptedKey) { result -> status = result.fold({ "连接成功" }, { "连接失败：${it.message ?: "未知错误"}" }) }
                }
                SmallAction("获取模型", Modifier.weight(1f), primary = true) {
                    status = "正在获取模型…"
                    viewModel.fetchModels(format, baseUrl, apiKey, encryptedKey) { result ->
                        result.onSuccess { fetched = it; status = "获取到 ${it.size} 个模型" }.onFailure { status = "获取失败：${it.message ?: "未知错误"}" }
                    }
                }
            }
            if (status.isNotBlank()) Text(status, fontSize = 9.5.sp, color = ArchiveColors.Tertiary, modifier = Modifier.padding(top = 6.dp))

            if (fetched.isNotEmpty()) {
                FieldLabel("可用模型", top = 18)
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(ArchiveColors.Surface)) {
                    fetched.take(60).forEach { id ->
                        val exists = models.any { it.modelId == id }
                        Row(Modifier.fillMaxWidth().clickable { if (!exists) models.add(ProviderModel(providerId.orEmpty(), id)) }.padding(horizontal = 12.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(id, fontSize = 10.5.sp, color = if (exists) ArchiveColors.Text else ArchiveColors.Secondary, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(if (exists) "已添加" else "+", fontSize = 10.sp, color = ArchiveColors.Tertiary)
                        }
                    }
                }
            }

            FieldLabel("模型", top = 18)
            if (models.isEmpty()) Text("还没有模型，获取模型或手动添加一个模型 ID", fontSize = 10.5.sp, color = ArchiveColors.Tertiary, modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(ArchiveColors.Surface).padding(16.dp))
            models.forEachIndexed { index, model ->
                Column(Modifier.fillMaxWidth().padding(bottom = 6.dp).clip(RoundedCornerShape(13.dp)).background(ArchiveColors.Surface).padding(horizontal = 11.dp, vertical = 10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(model.displayName.ifBlank { model.modelId }, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(model.modelId, fontSize = 9.sp, color = ArchiveColors.Tertiary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Text(if (model.vision) "识图 开" else "识图 关", fontSize = 9.sp, color = ArchiveColors.Secondary, modifier = Modifier.clip(CircleShape).background(Color.White).clickable { models[index] = model.copy(vision = !model.vision) }.padding(horizontal = 7.dp, vertical = 5.dp))
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.Rounded.DeleteOutline, null, tint = ArchiveColors.Tertiary, modifier = Modifier.size(22.dp).clickable { models.removeAt(index) }.padding(4.dp))
                    }
                    BasicTextField(value = model.displayName, onValueChange = { models[index] = model.copy(displayName = it) }, singleLine = true, textStyle = TextStyle(fontSize = 10.5.sp, color = ArchiveColors.Secondary), modifier = Modifier.fillMaxWidth().padding(top = 7.dp).clip(RoundedCornerShape(9.dp)).background(Color.White).padding(horizontal = 9.dp, vertical = 7.dp), decorationBox = { inner -> Box { if (model.displayName.isBlank()) Text("自定义显示名", fontSize = 10.5.sp, color = ArchiveColors.Tertiary); inner() } })
                }
            }
            Row(Modifier.fillMaxWidth().padding(top = 2.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                PlainInput(customModel, { customModel = it }, "手动输入模型 ID", Modifier.weight(1f))
                Box(Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(ArchiveColors.Dark).clickable(enabled = customModel.isNotBlank()) { if (models.none { it.modelId == customModel.trim() }) models.add(ProviderModel(providerId.orEmpty(), customModel.trim())); customModel = "" }, contentAlignment = Alignment.Center) { Icon(Icons.Rounded.Add, null, tint = Color.White, modifier = Modifier.size(16.dp)) }
            }

            if (providerId != null) {
                Text("删除供应商", color = ArchiveColors.Danger, fontSize = 11.sp, modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 28.dp, bottom = 30.dp).clickable { viewModel.deleteProvider(providerId, onSaved) }.padding(10.dp))
            } else Spacer(Modifier.height(30.dp))
        }
    }
}

@Composable private fun FieldLabel(text: String, top: Int = 0) { Text(text, fontSize = 10.5.sp, color = ArchiveColors.Secondary, fontWeight = FontWeight.Medium, modifier = Modifier.padding(top = top.dp, bottom = 6.dp)) }

@Composable
private fun PlainInput(value: String, onValueChange: (String) -> Unit, placeholder: String, modifier: Modifier = Modifier.fillMaxWidth(), password: Boolean = false) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        visualTransformation = if (password) PasswordVisualTransformation() else VisualTransformation.None,
        textStyle = TextStyle(fontSize = 12.sp, color = ArchiveColors.Text),
        modifier = modifier.clip(RoundedCornerShape(12.dp)).background(ArchiveColors.Surface).padding(horizontal = 12.dp, vertical = 11.dp),
        decorationBox = { inner -> Box { if (value.isBlank()) Text(placeholder, fontSize = 12.sp, color = ArchiveColors.Tertiary); inner() } },
    )
}

@Composable
private fun SmallAction(label: String, modifier: Modifier, primary: Boolean = false, onClick: () -> Unit) {
    Box(modifier.height(36.dp).clip(RoundedCornerShape(11.dp)).background(if (primary) ArchiveColors.Dark else ArchiveColors.Surface).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Text(label, fontSize = 10.5.sp, fontWeight = FontWeight.Medium, color = if (primary) Color.White else ArchiveColors.Text)
    }
}
