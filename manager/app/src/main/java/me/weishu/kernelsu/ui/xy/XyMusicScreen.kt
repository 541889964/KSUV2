package me.weishu.kernelsu.ui.xy
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
@Composable fun XyMusicScreen(onBack: () -> Unit) {
    var enabled by remember { mutableStateOf(DdBgmPlayer.enabled) }
    var loop by remember { mutableStateOf(DdBgmPlayer.loopOne) }
    var vol by remember { mutableStateOf(DdBgmPlayer.volume) }
    Column(Modifier.fillMaxSize().background(XyColor.bg)
        .verticalScroll(rememberScrollState()).padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text("←", color = XyColor.txt1, fontSize = 22.sp,
                modifier = Modifier.clip(RoundedCornerShape(10.dp))
                    .hapticClick { onBack() }.padding(10.dp))
            Spacer(Modifier.width(12.dp))
            Column {
                Text("背景音乐", color = XyColor.txt1, fontSize = 22.sp, fontWeight = FontWeight.W600)
                Text("内置 ${DdBgmPlayer.trackCount} 首 · 随机播放", color = XyColor.txt2, fontSize = 11.sp)
            }
        }
        Spacer(Modifier.height(24.dp))
        XyGroup("播放控制", XyColor.red) {
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                .background(XyColor.cardHigh).padding(12.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("启用背景音乐", color = XyColor.txt1, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Text("打开应用自动随机播放", color = XyColor.txt3, fontSize = 11.sp)
                }
                Switch(enabled, {
                    enabled = it; DdBgmPlayer.setEnabled(it)
                }, colors = SwitchDefaults.colors(
                    checkedTrackColor = XyColor.brand, checkedThumbColor = Color.White))
            }
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                .background(XyColor.cardHigh).padding(12.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("单曲循环", color = XyColor.txt1, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Text("当前曲目播完后重播", color = XyColor.txt3, fontSize = 11.sp)
                }
                Switch(loop, {
                    loop = it; DdBgmPlayer.loopOne = it
                }, colors = SwitchDefaults.colors(
                    checkedTrackColor = XyColor.brand, checkedThumbColor = Color.White))
            }
        }
        Spacer(Modifier.height(16.dp))
        XyGroup("当前播放") {
            Text(if (DdBgmPlayer.currentTrack.isEmpty()) "暂无播放" else DdBgmPlayer.currentTrack,
                color = XyColor.txt1, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Text(if (DdBgmPlayer.isPlaying) "播放中" else "已暂停",
                color = XyColor.txt3, fontSize = 11.sp)
        }
        Spacer(Modifier.height(16.dp))
        XyGroup("控制") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.weight(1f).height(44.dp).clip(RoundedCornerShape(10.dp))
                    .background(XyColor.cardHigh).hapticClick {
                        DdBgmPlayer.prev()
                    }, contentAlignment = Alignment.Center) {
                    Text("⏮ 上一首", color = XyColor.txt1, fontSize = 13.sp)
                }
                Box(Modifier.weight(1f).height(44.dp).clip(RoundedCornerShape(10.dp))
                    .background(Brush.horizontalGradient(listOf(XyColor.red, XyColor.red2)))
                    .hapticClick {
                        DdBgmPlayer.play()
                    }, contentAlignment = Alignment.Center) {
                    Text("⏭ 下一首", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        XyGroup("音量") {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("音量", color = XyColor.txt1, fontSize = 13.sp,
                        fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                    Text("%d%%".format((vol * 100).toInt()), color = XyColor.brand,
                        fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                Slider(value = vol, onValueChange = { v: Float ->
                    vol = v; DdBgmPlayer.volume = v
                }, valueRange = 0f..1f,
                    colors = SliderDefaults.colors(thumbColor = XyColor.brand,
                        activeTrackColor = XyColor.brand,
                        inactiveTrackColor = Color(0xFF2A3038)))
            }
        }
        Spacer(Modifier.height(40.dp))
    }
}
