package me.weishu.kernelsu.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun AppSplashScreen(onFinished: () -> Unit) {
    var startAnim by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(true) }
    val alpha by animateFloatAsState(if (startAnim) 1f else 0f, tween(1000), label = "alpha")
    val scale by animateFloatAsState(if (startAnim) 1f else 0.8f, spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "scale")
    LaunchedEffect(Unit) { startAnim = true; delay(2500); visible = false; onFinished() }
    if (visible) {
        Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF0D1B2A), Color(0xFF1B263B)))).clickable { visible = false; onFinished() }, contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("KernelSU", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6CB4EE), modifier = Modifier.scale(scale).alpha(alpha))
                Spacer(modifier = Modifier.height(16.dp))
                Text("点击跳过", fontSize = 14.sp, color = Color.Gray, modifier = Modifier.alpha(alpha))
            }
        }
    }
}
