package me.weishu.kernelsu.ui.xy
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.io.File
@Composable fun XyBootAnimScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var uri by remember { mutableStateOf<Uri?>(null) }
    var frames by remember { mutableStateOf<List<Bitmap>>(emptyList()) }
    var busy by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("") }
    var fps by remember { mutableIntStateOf(15) }
    var w by remember { mutableIntStateOf(1080) }
    var h by remember { mutableIntStateOf(2400) }
    var loop by remember { mutableIntStateOf(0) }
    val pick = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { u ->
        if (u != null) { uri = u; frames = emptyList(); status = "" }
    }
    Column(Modifier.fillMaxSize().background(XyColor.bg)
        .verticalScroll(rememberScrollState()).padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text("←", color=XyColor.txt1, fontSize=22.sp,
                modifier = Modifier.clip(RoundedCornerShape(10.dp))
                    .hapticClick { onBack() }.padding(10.dp))
            Spacer(Modifier.width(12.dp))
            Column {
                Text("开机动画", color=XyColor.txt1, fontSize=22.sp, fontWeight=FontWeight.W600)
                Text("选视频 → 自动抽帧 → 一键装模块", color=XyColor.txt2, fontSize=11.sp)
            }
        }
        Spacer(Modifier.height(24.dp))
        XyPrimaryBtn("选择本地视频") { pick.launch(arrayOf("video/*")) }
        uri?.let {
            Spacer(Modifier.height(12.dp))
            Text("已选: ${it.lastPathSegment ?: "视频"}", color=XyColor.txt2, fontSize=11.sp)
        }
        if (frames.isNotEmpty()) {
            Spacer(Modifier.height(14.dp))
            Text("预览（${frames.size} 帧）", color=XyColor.txt1, fontSize=12.sp)
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(frames.take(20)) { b ->
                    Image(b.asImageBitmap(), null,
                        modifier=Modifier.size(64.dp).clip(RoundedCornerShape(10.dp)))
                }
            }
        }
        Spacer(Modifier.height(22.dp))
        XyGroup("参数") {
            Row(verticalAlignment=Alignment.CenterVertically) {
                Text("帧率", color=XyColor.txt1, fontSize=13.sp, modifier=Modifier.weight(1f))
                Row(horizontalArrangement=Arrangement.spacedBy(6.dp)) {
                    listOf(10,15,24,30).forEach { f -> XyChip("$f", fps==f) { fps=f } }
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment=Alignment.CenterVertically) {
                Text("分辨率", color=XyColor.txt1, fontSize=13.sp, modifier=Modifier.weight(1f))
                Row(horizontalArrangement=Arrangement.spacedBy(6.dp)) {
                    listOf(720 to 1600, 1080 to 2400).forEach { (ww,hh) ->
                        XyChip("${ww}×${hh}", w==ww) { w=ww; h=hh }
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment=Alignment.CenterVertically) {
                Text("循环", color=XyColor.txt1, fontSize=13.sp, modifier=Modifier.weight(1f))
                Row(horizontalArrangement=Arrangement.spacedBy(6.dp)) {
                    listOf(0,1,2).forEach { l ->
                        XyChip(if(l==0) "无限" else "${l}次", loop==l) { loop=l }
                    }
                }
            }
        }
        Spacer(Modifier.height(22.dp))
        if (frames.isEmpty()) {
            XyPrimaryBtn("① 解析视频成帧") {
                val u = uri ?: return@XyPrimaryBtn
                busy = true; status = "抽帧中…"
                scope.launch {
                    val fs = XyVideo2Boot.extractFrames(ctx, u, fps, w, h)
                    frames = fs; busy = false
                    status = if (fs.isEmpty()) "抽帧失败" else "已抽 ${fs.size} 帧"
                }
            }
        } else {
            XyPrimaryBtn("② 生成模块并安装") {
                busy = true; status = "打包中…"
                scope.launch {
                    val tmp = File(ctx.cacheDir, "xy_boot"); tmp.mkdirs()
                    val ba = File(tmp, "bootanimation.zip")
                    if (!XyVideo2Boot.packBootAnim(frames, ba, fps, loop)) {
                        busy=false; status="打包失败"; return@launch
                    }
                    val mod = File(tmp, "xy_boot_module.zip")
                    if (!XyVideo2Boot.packKsuModule(ba, mod, "xy_boot_anim", "XuanYi 开机动画")) {
                        busy=false; status="模块打包失败"; return@launch
                    }
                    status = "安装中…"
                    val (_,msg) = XyVideo2Boot.installModule(mod.absolutePath)
                    busy = false; status = msg
                }
            }
            Spacer(Modifier.height(12.dp))
            Box(Modifier.fillMaxWidth().height(46.dp).clip(RoundedCornerShape(12.dp))
                .background(XyColor.cardHigh).hapticClick { frames = emptyList() },
                contentAlignment=Alignment.Center) {
                Text("重新选择", color=XyColor.txt2, fontSize=13.sp)
            }
        }
        if (busy) {
            Spacer(Modifier.height(16.dp))
            CircularProgressIndicator(color=XyColor.red,
                modifier=Modifier.align(Alignment.CenterHorizontally))
        }
        if (status.isNotEmpty()) {
            Spacer(Modifier.height(14.dp))
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                .background(XyColor.card).padding(14.dp)) {
                Text(status, color=XyColor.txt1, fontSize=12.sp)
            }
        }
        Spacer(Modifier.height(40.dp))
    }
}
