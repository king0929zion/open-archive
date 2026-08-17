package com.king0929zion.openarchive.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.SentimentSatisfied
import androidx.compose.material.icons.rounded.WbCloudy
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
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
import com.king0929zion.openarchive.data.DraftSnapshot
import com.king0929zion.openarchive.data.NearbyPlace
import com.king0929zion.openarchive.ui.components.ArchivePill
import com.king0929zion.openarchive.ui.components.DemoOrRemoteImage
import com.king0929zion.openarchive.ui.theme.ArchiveColors
import kotlin.math.roundToInt

private val composePlaces = listOf(
    NearbyPlace("上海 · 武康路", "武康路", "街区", "120 m", "徐汇区武康路 · 近湖南路"),
    NearbyPlace("上海 · 安福路", "安福路", "街区", "260 m", "徐汇区安福路 · 近乌鲁木齐中路"),
    NearbyPlace("上海 · %Arabica 武康庭", "%Arabica 武康庭", "咖啡", "310 m", "武康路376号武康庭"),
    NearbyPlace("上海 · 上海图书馆", "上海图书馆", "文化", "640 m", "淮海中路1555号"),
    NearbyPlace("上海 · 徐家汇公园", "徐家汇公园", "公园", "900 m", "肇嘉浜路889号"),
)
private val weatherOptions = listOf("sunny" to "晴朗", "cloudy" to "多云", "overcast" to "阴天", "rain" to "小雨", "snow" to "下雪")
private val moodOptions = listOf("low" to "低落", "calm" to "平静", "cozy" to "悠闲", "happy" to "开心", "energy" to "活力")

@Composable
fun ComposeScreen(viewModel: ArchiveViewModel, onClose: () -> Unit, onPublished: (String) -> Unit) {
    val draft by viewModel.draft.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var locationOpen by remember { mutableStateOf(false) }
    var weatherOpen by remember { mutableStateOf(false) }
    var moodOpen by remember { mutableStateOf(false) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(9)) { uris ->
        uris.forEach { uri ->
            runCatching { context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
        }
        viewModel.addImages(uris.map { it.toString() })
    }

    BackHandler(locationOpen || weatherOpen || moodOpen) {
        locationOpen = false; weatherOpen = false; moodOpen = false
    }

    Box(Modifier.fillMaxSize().background(Color.White)) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(start = 22.dp, end = 22.dp, top = 72.dp, bottom = 28.dp)
        ) {
            BasicTextField(
                value = draft.text,
                onValueChange = { text -> viewModel.updateDraft { it.copy(text = text) } },
                textStyle = TextStyle(color = ArchiveColors.Text, fontSize = 15.sp, lineHeight = 24.sp),
                modifier = Modifier.fillMaxWidth().heightIn(min = 68.dp, max = 420.dp),
                decorationBox = { inner ->
                    Box {
                        if (draft.text.isEmpty()) Text("写下此刻…", color = ArchiveColors.Tertiary, fontSize = 15.sp)
                        inner()
                    }
                },
            )
            PhotoStrip(draft, onAdd = {
                picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }, onRemove = viewModel::removeImage)

            Row(
                Modifier.fillMaxWidth().padding(top = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                verticalAlignment = Alignment.Top,
            ) {
                ArchivePill(
                    label = draft.location.ifBlank { "位置" },
                    icon = { Icon(Icons.Rounded.LocationOn, null, Modifier.size(14.dp)) },
                    selected = draft.location.isNotBlank(),
                ) { weatherOpen = false; moodOpen = false; locationOpen = true }
                Box {
                    ArchivePill(
                        label = ArchiveViewModel.weatherLabel(draft.weather).ifBlank { "天气" },
                        icon = { Icon(Icons.Rounded.WbCloudy, null, Modifier.size(14.dp)) },
                        selected = draft.weather.isNotBlank(),
                    ) { locationOpen = false; moodOpen = false; weatherOpen = !weatherOpen }
                    if (weatherOpen) WeatherPopup(draft.weather, onSelect = { key ->
                        viewModel.updateDraft { it.copy(weather = if (it.weather == key) "" else key) }
                        weatherOpen = false
                    }, onDismiss = { weatherOpen = false })
                }
                Box {
                    ArchivePill(
                        label = ArchiveViewModel.moodLabel(draft.mood).ifBlank { "心情" },
                        icon = { Icon(Icons.Rounded.SentimentSatisfied, null, Modifier.size(14.dp)) },
                        selected = draft.mood.isNotBlank(),
                    ) { locationOpen = false; weatherOpen = false; moodOpen = !moodOpen }
                    if (moodOpen) MoodPopup(draft.mood, onSelect = { key ->
                        viewModel.updateDraft { it.copy(mood = key) }
                    }, onClear = {
                        viewModel.updateDraft { it.copy(mood = "") }; moodOpen = false
                    }, onDismiss = { moodOpen = false })
                }
            }
        }

        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            RoundAction(false, onClose) { Icon(Icons.Rounded.Close, null, Modifier.size(18.dp)) }
            RoundAction(draft.text.isNotBlank() || draft.images.isNotEmpty(), onClick = {
                viewModel.publish(onPublished)
            }) { Icon(Icons.Rounded.Check, null, Modifier.size(18.dp)) }
        }

        if (locationOpen) {
            LocationSheet(
                current = draft.location,
                onSelect = { value ->
                    viewModel.updateDraft { it.copy(location = value) }
                    locationOpen = false
                },
                onDismiss = { locationOpen = false },
            )
        }
    }
}

@Composable
private fun RoundAction(enabled: Boolean, onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(
        Modifier.size(38.dp).clip(CircleShape)
            .background(if (enabled) ArchiveColors.Dark else ArchiveColors.Surface)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        androidx.compose.runtime.CompositionLocalProvider(
            androidx.compose.material3.LocalContentColor provides if (enabled) Color.White else ArchiveColors.Text,
            content = content,
        )
    }
}

@Composable
private fun PhotoStrip(draft: DraftSnapshot, onAdd: () -> Unit, onRemove: (Int) -> Unit) {
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        draft.images.forEachIndexed { index, uri ->
            Box(Modifier.size(70.dp)) {
                DemoOrRemoteImage(uri, Modifier.fillMaxSize().clip(RoundedCornerShape(13.dp)))
                Box(
                    Modifier.align(Alignment.TopEnd).padding(4.dp).size(18.dp).clip(CircleShape)
                        .background(Color.Black.copy(alpha = .42f)).clickable { onRemove(index) },
                    contentAlignment = Alignment.Center,
                ) { Text("×", color = Color.White, fontSize = 12.sp) }
            }
        }
        if (draft.images.size < 9) {
            Box(
                Modifier.size(70.dp).clip(RoundedCornerShape(13.dp)).background(Color(0xFFF6F6F6)).clickable(onClick = onAdd),
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Rounded.AddPhotoAlternate, null, tint = ArchiveColors.Secondary, modifier = Modifier.size(20.dp)) }
        }
    }
}

@Composable
private fun WeatherPopup(selected: String, onSelect: (String) -> Unit, onDismiss: () -> Unit) {
    Popup(
        alignment = Alignment.TopStart,
        offset = IntOffset(0, 42),
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true),
    ) {
        Column(
            Modifier.width(218.dp).shadow(5.dp, RoundedCornerShape(16.dp), ambientColor = Color.Black.copy(.035f), spotColor = Color.Black.copy(.035f))
                .background(Color.White, RoundedCornerShape(16.dp)).padding(10.dp)
        ) {
            weatherOptions.chunked(3).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    row.forEach { (key, label) ->
                        Box(
                            Modifier.weight(1f).height(38.dp).clip(RoundedCornerShape(10.dp))
                                .background(if (key == selected) ArchiveColors.Dark else ArchiveColors.Surface)
                                .clickable { onSelect(key) },
                            contentAlignment = Alignment.Center,
                        ) { Text(label, fontSize = 10.sp, color = if (key == selected) Color.White else ArchiveColors.Secondary) }
                    }
                    repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                }
                Spacer(Modifier.height(5.dp))
            }
        }
    }
}

@Composable
private fun MoodPopup(selected: String, onSelect: (String) -> Unit, onClear: () -> Unit, onDismiss: () -> Unit) {
    val initial = moodOptions.indexOfFirst { it.first == selected }.let { if (it < 0) 1 else it }
    var value by remember(selected) { mutableFloatStateOf(initial.toFloat()) }
    Popup(
        alignment = Alignment.TopEnd,
        offset = IntOffset(0, 42),
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true),
    ) {
        Column(
            Modifier.width(248.dp).shadow(5.dp, RoundedCornerShape(16.dp), ambientColor = Color.Black.copy(.035f), spotColor = Color.Black.copy(.035f))
                .background(Color.White, RoundedCornerShape(16.dp)).padding(horizontal = 13.dp, vertical = 11.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(moodOptions[value.roundToInt().coerceIn(0, 4)].second, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Text("清除", fontSize = 10.sp, color = ArchiveColors.Tertiary, modifier = Modifier.clickable(onClick = onClear))
            }
            Slider(
                value = value,
                onValueChange = {
                    value = it
                    onSelect(moodOptions[it.roundToInt().coerceIn(0, 4)].first)
                },
                valueRange = 0f..4f,
                steps = 3,
                colors = SliderDefaults.colors(
                    thumbColor = ArchiveColors.Dark,
                    activeTrackColor = Color(0xFFE9E9E9),
                    inactiveTrackColor = Color(0xFFE9E9E9),
                    activeTickColor = Color.Transparent,
                    inactiveTickColor = Color.Transparent,
                ),
                modifier = Modifier.fillMaxWidth().height(30.dp),
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                moodOptions.forEach { Text(it.second, fontSize = 8.sp, color = Color(0xFFB5B5B5)) }
            }
        }
    }
}

@Composable
private fun LocationSheet(current: String, onSelect: (String) -> Unit, onDismiss: () -> Unit) {
    var query by remember(current) { mutableStateOf(current) }
    val q = query.trim().lowercase()
    val filtered = if (q.isBlank()) composePlaces else composePlaces.filter {
        listOf(it.value, it.name, it.category, it.address).joinToString(" ").lowercase().contains(q)
    }
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = .20f)).clickable(onClick = onDismiss))
    Column(
        Modifier.fillMaxWidth().align(Alignment.BottomCenter).clip(RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp))
            .background(Color.White).clickable(enabled = false) {}.padding(bottom = 22.dp)
    ) {
        Box(Modifier.fillMaxWidth().padding(top = 9.dp), contentAlignment = Alignment.Center) {
            Box(Modifier.width(32.dp).height(3.dp).clip(CircleShape).background(Color(0xFFE8E8E8)))
        }
        Text("位置", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 18.dp, top = 12.dp, bottom = 12.dp))
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp).clip(RoundedCornerShape(15.dp)).background(Color(0xFFF6F6F6)).padding(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BasicTextField(
                value = query,
                onValueChange = { query = it },
                singleLine = true,
                textStyle = TextStyle(fontSize = 13.sp, color = ArchiveColors.Text),
                modifier = Modifier.weight(1f).padding(horizontal = 7.dp),
                decorationBox = { inner -> Box { if (query.isBlank()) Text("搜索或输入位置", color = ArchiveColors.Tertiary, fontSize = 13.sp); inner() } },
            )
            Box(Modifier.clip(CircleShape).background(ArchiveColors.Dark).clickable { if (query.isNotBlank()) onSelect(query.trim()) }.padding(horizontal = 12.dp, vertical = 7.dp)) {
                Text("使用", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        Text("附近地点", fontSize = 10.sp, color = ArchiveColors.Tertiary, modifier = Modifier.padding(start = 20.dp, top = 10.dp, bottom = 4.dp))
        Column(Modifier.fillMaxWidth().heightIn(max = 300.dp).verticalScroll(rememberScrollState()).padding(horizontal = 12.dp)) {
            filtered.take(5).forEach { place ->
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                        .background(if (current == place.value) Color(0xFFF6F6F6) else Color.Transparent)
                        .clickable { onSelect(place.value) }.padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.size(28.dp).clip(RoundedCornerShape(9.dp)).background(Color(0xFFF4F4F4)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.LocationOn, null, tint = ArchiveColors.Secondary, modifier = Modifier.size(13.dp))
                    }
                    Spacer(Modifier.width(9.dp))
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(place.name, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.width(6.dp)); Text(place.category, fontSize = 9.sp, color = ArchiveColors.Tertiary)
                        }
                        Text(place.address, fontSize = 9.5.sp, color = Color(0xFFAAAAAA), maxLines = 1)
                    }
                    Text(place.distance, fontSize = 9.sp, color = Color(0xFFB8B8B8))
                }
            }
            if (filtered.isEmpty()) Text("没有匹配地点，可以直接使用输入内容", color = ArchiveColors.Tertiary, fontSize = 11.sp, modifier = Modifier.padding(16.dp))
        }
        Text("不记录位置", color = ArchiveColors.Tertiary, fontSize = 11.sp, modifier = Modifier.align(Alignment.CenterHorizontally).clickable { onSelect("") }.padding(10.dp))
    }
}
