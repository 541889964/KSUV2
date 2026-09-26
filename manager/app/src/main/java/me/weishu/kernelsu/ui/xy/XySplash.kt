package me.weishu.kernelsu.ui.xy
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.sin
import kotlin.random.Random
private class XSp(var x:Float,var y:Float,val r:Float,val base:Float,
                  val ph:Float,val sp:Float,val dr:Float,var lx:Float,var ly:Float)
@Composable fun XySplash(onFinish: () -> Unit) {
    var start by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { start = true; delay(2200); onFinish() }
    var tick by remember { mutableIntStateOf(0) }
    val sps = remember {
        val n = if (Runtime.getRuntime().maxMemory() < 192L*1024*1024) 25 else 70
        List(n) {
            val x = Random.nextFloat(); val y = Random.nextFloat()
            XSp(x,y,Random.nextFloat()*1.6f+0.4f,Random.nextFloat()*0.6f+0.2f,
                Random.nextFloat()*6.28f,Random.nextFloat()*0.6f+0.3f,
                (Random.nextFloat()-0.5f)*0.02f,x,y)
        }
    }
    LaunchedEffect(Unit) {
        var last = 0L
        while (true) { withFrameNanos { now ->
            if (last == 0L) { last = now; return@withFrameNanos }
            last = now
            for (s in sps) {
                s.lx = s.x; s.ly = s.y
                s.y -= 0.00012f; s.x += s.dr * 0.001f
                if (s.y < -0.05f) { s.y = 1.05f; s.x = Random.nextFloat() }
                if (s.x < -0.05f) s.x = 1.05f
                if (s.x > 1.05f) s.x = -0.05f
            }
            tick++
        } }
    }
    val inf = rememberInfiniteTransition(label="sp")
    val ha by inf.animateFloat(0.22f,0.65f,
        infiniteRepeatable(tween(1600, easing=LinearEasing), RepeatMode.Reverse), label="ha")
    val hs by inf.animateFloat(0.88f,1.18f,
        infiniteRepeatable(tween(1600, easing=LinearEasing), RepeatMode.Reverse), label="hs")
    val rr by inf.animateFloat(0f,360f,
        infiniteRepeatable(tween(4000, easing=LinearEasing)), label="rr")
    val ls by animateFloatAsState(if(start) 1f else 0.4f,
        spring(Spring.DampingRatioMediumBouncy,Spring.StiffnessLow), label="ls")
    val la by animateFloatAsState(if(start) 1f else 0f, tween(900), label="la")
    val ta by animateFloatAsState(if(start) 1f else 0f, tween(800,delayMillis=400), label="ta")
    val ts by animateFloatAsState(if(start) 1f else 0.88f, spring(stiffness=900f), label="ts")
    val sa by animateFloatAsState(if(start) 1f else 0f, tween(700,delayMillis=700), label="sa")
    val laa by animateFloatAsState(if(start) 1f else 0f, tween(600,delayMillis=1100), label="laa")
    val pb by animateFloatAsState(if(start) 1f else 0f, tween(1800,delayMillis=400), label="pb")
    Box(Modifier.fillMaxSize().background(
        Brush.radialGradient(listOf(Color(0xFF1A0A0F), Color(0xFF06070B)), radius=1400f))) {
        Canvas(Modifier.fillMaxSize()) {
            tick
            val w = size.width; val h = size.height
            for (s in sps) {
                val sx = s.x*w; val sy = s.y*h
                val lx = s.lx*w; val ly = s.ly*h
                val tw = sin(tick*0.02f*s.sp+s.ph)*0.35f+0.65f
                val a = s.base*tw
                drawLine(Color(0xFFFF6B81).copy(alpha=a*0.55f),
                    Offset(lx,ly),Offset(sx,sy),s.r*0.9f,StrokeCap.Round)
                drawCircle(Color(0xFFFFE7EC).copy(alpha=a*0.15f),s.r*6f,Offset(sx,sy))
                drawCircle(Color(0xFFFFFFFF).copy(alpha=a*0.55f),s.r*2f,Offset(sx,sy))
                drawCircle(Color(0xFFFFC0C8).copy(alpha=a),s.r,Offset(sx,sy))
            }
        }
        Box(Modifier.fillMaxSize(), contentAlignment=Alignment.Center) {
            Column(horizontalAlignment=Alignment.CenterHorizontally,
                verticalArrangement=Arrangement.Center) {
                Box(contentAlignment=Alignment.Center) {
                    Box(Modifier.size(260.dp).scale(hs).alpha(ha*la)
                        .background(Brush.radialGradient(listOf(
                            Color(0xFFEB0028).copy(alpha=0.55f),
                            Color(0xFFFF1744).copy(alpha=0.15f),
                            Color.Transparent)), RoundedCornerShape(130.dp)))
                    Canvas(Modifier.size(190.dp).alpha(la*0.75f)) {
                        val r = size.minDimension/2 - 6f
                        val cx = size.width/2; val cy = size.height/2
                        rotate(rr, Offset(cx,cy)) {
                            for (i in 0 until 4) {
                                drawArc(Brush.sweepGradient(listOf(
                                    Color(0xFFFF1744),Color(0xFFEB0028).copy(alpha=0.2f),
                                    Color(0xFFFF1744))),
                                    i*90f+10f, 60f, false, Offset(cx-r,cy-r),
                                    androidx.compose.ui.geometry.Size(r*2,r*2),
                                    style = Stroke(2.5f, cap=StrokeCap.Round))
                            }
                        }
                    }
                    Box(Modifier.size(124.dp).scale(ls).alpha(la)
                        .background(Brush.linearGradient(listOf(
                            Color(0xFFFF1744),Color(0xFFEB0028),Color(0xFFC4001D))),
                            RoundedCornerShape(34.dp)),
                        contentAlignment=Alignment.Center) {
                        Text("X", color=Color.White, fontSize=70.sp,
                            fontWeight=FontWeight.Black, letterSpacing=(-2).sp)
                    }
                }
                Spacer(Modifier.height(46.dp))
                Text("XuanYi Ksu",
                    style=TextStyle(brush=Brush.linearGradient(listOf(
                        Color.White,Color(0xFFFFD5DD),Color(0xFFFF6B81))),
                        fontSize=32.sp, fontWeight=FontWeight.Bold, letterSpacing=1.5.sp),
                    modifier=Modifier.alpha(ta).scale(ts))
                Spacer(Modifier.height(12.dp))
                Text("DEEP SPACE EDITION", color=Color(0xFF9BA3AD),
                    fontSize=10.sp, letterSpacing=6.sp,
                    fontWeight=FontWeight.Medium, modifier=Modifier.alpha(sa))
                Spacer(Modifier.height(72.dp))
                Box(Modifier.width(160.dp).height(2.dp).alpha(laa)
                    .clip(RoundedCornerShape(1.dp)).background(Color(0xFF1F242D))) {
                    Box(Modifier.fillMaxWidth(pb).height(2.dp)
                        .background(Brush.horizontalGradient(listOf(
                            Color(0xFFEB0028),Color(0xFFFF6B81),Color(0xFFFF1744)))))
                }
                Spacer(Modifier.height(16.dp))
                Text("powered by KernelSU", color=Color(0xFF4A5058),
                    fontSize=10.sp, letterSpacing=1.sp, modifier=Modifier.alpha(laa))
            }
        }
    }
}
