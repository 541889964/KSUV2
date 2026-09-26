package me.weishu.kernelsu.ui.xy
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
@Composable fun DdOverlay(content: @Composable () -> Unit) {
    val ctx = LocalContext.current
    var splash by remember { mutableStateOf(true) }
    var showBoot by remember { mutableStateOf(false) }
    var showSys by remember { mutableStateOf(false) }
    var showMusic by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        XyPrefs.load(ctx)
        DdBgmPlayer.init(ctx)
        DdBgmPlayer.play()
    }
    if (showBoot) { XyBootAnimScreen { showBoot = false }; return }
    if (showSys) { XySysScreen { showSys = false }; return }
    if (showMusic) { XyMusicScreen { showMusic = false }; return }
    Box(Modifier.fillMaxSize()) {
        XyWallpaperLayer()
        XyStealthGate {
            Column(Modifier.fillMaxSize()) {
                XyStatusBanner()
                Box(Modifier.fillMaxSize()) { content() }
            }
        }
        XyParticleLayer(XyPrefs.config.particle)
        Box(Modifier.fillMaxSize().padding(24.dp)) {
            Box(Modifier.align(Alignment.BottomStart)) {
                FloatingBtn("🎵", Color(0xCC1F1F24)) { showMusic = true }
            }
            Box(Modifier.align(Alignment.BottomStart).padding(start = 68.dp)) {
                FloatingBtn("🎬", Color(0xCC17181C)) { showBoot = true }
            }
            Box(Modifier.align(Alignment.BottomStart).padding(start = 136.dp)) {
                FloatingBtn("⚙", Color(0xCC774F6F)) { showSys = true }
            }
        }
        XyAnnouncement()
        if (splash) XySplash { splash = false }
    }
}
@Composable
private fun FloatingBtn(emoji: String, color: Color, onClick: () -> Unit) {
    val inf = rememberInfiniteTransition(label = "fb")
    val glow by inf.animateFloat(0.35f, 0.85f,
        infiniteRepeatable(tween(1800, easing = LinearEasing), RepeatMode.Reverse), label = "g")
    Box(contentAlignment = Alignment.Center) {
        Box(Modifier.size(62.dp).alpha(glow * 0.35f).clip(CircleShape).background(color))
        Box(Modifier.size(48.dp).clip(CircleShape)
            .background(Brush.linearGradient(listOf(color, color.copy(alpha = 0.8f))))
            .hapticClick(scaleDown = 0.85f) { onClick() },
            contentAlignment = Alignment.Center) {
            Text(emoji, fontSize = 22.sp)
        }
    }
}
