package me.weishu.kernelsu.ui.xy
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
@Composable fun XyAnnouncement() {
    val ctx = LocalContext.current
    var text by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("公告") }
    var show by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        try {
            ctx.assets.open("announcement.txt").use { ins ->
                val raw = ins.bufferedReader().readText().trim()
                val lines = raw.split("\n", limit = 2)
                if (lines[0].startsWith("title:")) {
                    title = lines[0].substring(6).trim()
                    text = if (lines.size > 1) lines[1].trim() else ""
                } else text = raw
            }
        } catch (_: Exception) { text = "" }
        if (text.isNotEmpty()) {
            val sp = ctx.getSharedPreferences("xy_ann", Context.MODE_PRIVATE)
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            if (sp.getString("last","") != today) {
                show = true
                sp.edit().putString("last",today).apply()
            }
        }
    }
    if (!show) return
    Dialog(onDismissRequest = { show = false }) {
        Box(Modifier.fillMaxWidth().heightIn(max = 580.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(Color(0xFF10131A))
            .border(1.dp, Brush.linearGradient(listOf(
                Color(0xFFFF1744).copy(alpha=0.5f),Color.Transparent,
                Color(0xFFFF6B81).copy(alpha=0.35f))),
                RoundedCornerShape(26.dp)).padding(22.dp)) {
            Column(horizontalAlignment=Alignment.CenterHorizontally,
                modifier=Modifier.fillMaxWidth()) {
                Box(Modifier.size(60.dp).clip(RoundedCornerShape(20.dp))
                    .background(Brush.linearGradient(listOf(
                        Color(0xFFFF1744),Color(0xFFEB0028),Color(0xFFC4001D))),
                        RoundedCornerShape(20.dp)),
                    contentAlignment=Alignment.Center) {
                    Text("X", color=Color.White, fontSize=30.sp, fontWeight=FontWeight.Black)
                }
                Spacer(Modifier.height(16.dp))
                Text(title, color=Color(0xFFF5F7FA), fontSize=21.sp, fontWeight=FontWeight.Bold)
                Spacer(Modifier.height(18.dp))
                Box(Modifier.fillMaxWidth().heightIn(max = 320.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Brush.verticalGradient(listOf(
                        Color(0xFF1A1E26), Color(0xFF161A22))))
                    .padding(16.dp).verticalScroll(rememberScrollState())) {
                    Text(text, color=Color(0xFFE6EDF3), fontSize=13.sp, lineHeight=21.sp)
                }
                Spacer(Modifier.height(22.dp))
                XyPrimaryBtn("我知道了") { show = false }
            }
        }
    }
}
