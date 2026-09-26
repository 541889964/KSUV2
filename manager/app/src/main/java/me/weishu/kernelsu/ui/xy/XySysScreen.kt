package me.weishu.kernelsu.ui.xy
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
@Composable fun XySysScreen(onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var hasRoot by remember { mutableStateOf<Boolean?>(null) }
    var cpus by remember { mutableIntStateOf(8) }
    var curFreqs by remember { mutableStateOf<List<Int>>(emptyList()) }
    var maxFreqs by remember { mutableStateOf<List<Int>>(emptyList()) }
    var govs by remember { mutableStateOf<List<String>>(emptyList()) }
    var gpuCur by remember { mutableIntStateOf(0) }
    var gpuMax by remember { mutableIntStateOf(0) }
    var gpuGov by remember { mutableStateOf("") }
    var ios by remember { mutableStateOf<Map<String,String>>(emptyMap()) }
    var swappiness by remember { mutableIntStateOf(60) }
    var dirtyR by remember { mutableIntStateOf(20) }
    var dirtyB by remember { mutableIntStateOf(10) }
    var profile by remember { mutableStateOf(XySysTuner.Profile.BALANCE) }
    var status by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    fun refresh() { scope.launch {
        hasRoot = XySysTuner.hasRoot()
        if (hasRoot != true) { status = "未获取 root"; return@launch }
        cpus = XySysTuner.cpuCount()
        val cf = mutableListOf<Int>(); val mf = mutableListOf<Int>(); val gv = mutableListOf<String>()
        for (i in 0 until cpus) { cf.add(XySysTuner.cpuCurFreq(i)); mf.add(XySysTuner.cpuMaxFreq(i)); gv.add(XySysTuner.cpuGovernor(i)) }
        curFreqs = cf; maxFreqs = mf; govs = gv
        gpuCur = XySysTuner.gpuCurFreq(); gpuMax = XySysTuner.gpuMaxFreq(); gpuGov = XySysTuner.gpuGovernor()
        val m = mutableMapOf<String,String>()
        for (d in XySysTuner.blockDevices()) m[d] = XySysTuner.ioScheduler(d)
        ios = m
        swappiness = XySysTuner.swappiness(); dirtyR = XySysTuner.dirtyRatio(); dirtyB = XySysTuner.dirtyBgRatio()
    } }
    LaunchedEffect(Unit) { refresh() }
    LaunchedEffect(hasRoot) {
        if (hasRoot != true) return@LaunchedEffect
        while (true) { delay(1500)
            val cf = mutableListOf<Int>()
            for (i in 0 until cpus) cf.add(XySysTuner.cpuCurFreq(i))
            curFreqs = cf; gpuCur = XySysTuner.gpuCurFreq()
        }
    }
    Column(Modifier.fillMaxSize().background(XyColor.bg)
        .verticalScroll(rememberScrollState()).padding(20.dp)) {
        Row(verticalAlignment=Alignment.CenterVertically, modifier=Modifier.fillMaxWidth()) {
            Text("←", color=XyColor.txt1, fontSize=22.sp,
                modifier=Modifier.clip(RoundedCornerShape(10.dp)).hapticClick{onBack()}.padding(10.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("系统优化", color=XyColor.txt1, fontSize=22.sp, fontWeight=FontWeight.W600)
                Text("内核级调优 · 风驰式加速", color=XyColor.txt2, fontSize=11.sp)
            }
            Text("↻", color=XyColor.txt1, fontSize=22.sp,
                modifier=Modifier.clip(RoundedCornerShape(10.dp)).hapticClick{refresh()}.padding(10.dp))
        }
        Spacer(Modifier.height(20.dp))
        if (hasRoot == false) {
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                .background(Brush.linearGradient(listOf(Color(0xFF4A0A15), Color(0xFF2A0508))))
                .padding(18.dp)) {
                Text("未获取 root 权限\n请在 KSU 管理器给本应用授权",
                    color=Color(0xFFFF9EA8), fontSize=13.sp, lineHeight=20.sp)
            }
            return@Column
        }
        if (hasRoot == null) {
            CircularProgressIndicator(color=XyColor.red,
                modifier=Modifier.align(Alignment.CenterHorizontally))
            return@Column
        }
        XyGroup("调度模式", XyColor.red) {
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                XySysTuner.Profile.entries.forEach { p ->
                    Box(Modifier.weight(1f).height(46.dp).clip(RoundedCornerShape(12.dp))
                        .background(if(profile==p) Brush.horizontalGradient(listOf(
                            XyColor.red, XyColor.red2))
                        else Brush.linearGradient(listOf(Color(0xFF232831), Color(0xFF1C2028))))
                        .hapticClick {
                            profile = p
                            scope.launch {
                                busy = true; status = "应用 ${p.label}…"
                                XySysTuner.applyProfile(p)
                                busy = false; status = "✓ 已应用 ${p.label}"
                                refresh()
                            }
                        },
                        contentAlignment=Alignment.Center) {
                        Text(p.label, color=Color.White, fontSize=13.sp,
                            fontWeight=if(profile==p) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        XyGroup("CPU 实时频率") {
            Text("${cpus} 核 · 上限 ${maxFreqs.maxOrNull() ?: 0} MHz · 调度 ${govs.firstOrNull() ?: "-"}",
                color=XyColor.txt2, fontSize=11.sp)
            Spacer(Modifier.height(12.dp))
            curFreqs.forEachIndexed { i, f ->
                val mx = maxFreqs.getOrNull(i) ?: 1
                val r = if (mx>0) (f.toFloat()/mx).coerceIn(0f,1f) else 0f
                Row(verticalAlignment=Alignment.CenterVertically,
                    modifier=Modifier.fillMaxWidth().padding(vertical=3.dp)) {
                    Text("CPU$i", color=XyColor.txt2, fontSize=11.sp,
                        modifier=Modifier.width(52.dp), fontWeight=FontWeight.Medium)
                    Box(Modifier.weight(1f).height(7.dp).clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF232831))) {
                        Box(Modifier.fillMaxWidth(r).height(7.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Brush.horizontalGradient(listOf(
                                XyColor.brand, XyColor.red))))
                    }
                    Text("$f", color=XyColor.txt1, fontSize=11.sp,
                        modifier=Modifier.width(64.dp), textAlign=TextAlign.End,
                        fontWeight=FontWeight.Medium)
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        XyGroup("GPU") {
            Text("当前 ${gpuCur} MHz · 上限 ${gpuMax} MHz · $gpuGov",
                color=XyColor.txt2, fontSize=11.sp)
            Spacer(Modifier.height(10.dp))
            val mx = if (gpuMax>0) gpuMax else 900
            Slider(value=gpuMax.toFloat(),
                onValueChange = { v: Float -> gpuMax = v.toInt() },
                onValueChangeFinished = {
                    scope.launch { XySysTuner.setGpuMaxFreq(gpuMax); status="✓ GPU → $gpuMax MHz"; refresh() }
                },
                valueRange=200f..mx.toFloat(),
                colors=SliderDefaults.colors(thumbColor=XyColor.brand,
                    activeTrackColor=XyColor.brand,
                    inactiveTrackColor=Color(0xFF2A3038)))
        }
        Spacer(Modifier.height(16.dp))
        if (ios.isNotEmpty()) {
            XyGroup("I/O 调度") {
                ios.forEach { (d, cur) ->
                    Row(verticalAlignment=Alignment.CenterVertically,
                        modifier=Modifier.fillMaxWidth().padding(vertical=5.dp)) {
                        Text("/dev/$d", color=XyColor.txt1, fontSize=12.sp,
                            modifier=Modifier.weight(1f), fontWeight=FontWeight.Medium)
                        Text(cur, color=XyColor.brand, fontSize=11.sp,
                            fontWeight=FontWeight.SemiBold,
                            modifier=Modifier.clip(RoundedCornerShape(8.dp))
                                .background(XyColor.brand.copy(alpha=0.2f))
                                .hapticClick { scope.launch {
                                    val list = XySysTuner.ioSchedulers(d)
                                    val next = list.getOrNull(
                                        (list.indexOf(cur)+1)%list.size.coerceAtLeast(1)) ?: "none"
                                    XySysTuner.setIoScheduler(d,next); status="✓ $d → $next"; refresh()
                                } }.padding(horizontal=10.dp, vertical=5.dp))
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
        XyGroup("内存参数") {
            XySliderRow("Swappiness", swappiness.toFloat(), 0f..200f,
                { v: Float -> swappiness = v.toInt() },
                { scope.launch { XySysTuner.setSwappiness(swappiness); status = "✓ Swappiness → $swappiness" } })
            XySliderRow("Dirty Ratio", dirtyR.toFloat(), 5f..50f,
                { v: Float -> dirtyR = v.toInt() },
                { scope.launch { XySysTuner.setDirtyRatio(dirtyR); status = "✓ Dirty Ratio → $dirtyR" } })
            XySliderRow("Dirty Background", dirtyB.toFloat(), 1f..30f,
                { v: Float -> dirtyB = v.toInt() },
                { scope.launch { XySysTuner.setDirtyBgRatio(dirtyB); status = "✓ Dirty BG → $dirtyB" } })
        }
        Spacer(Modifier.height(24.dp))
        if (busy) {
            CircularProgressIndicator(color=XyColor.red,
                modifier=Modifier.align(Alignment.CenterHorizontally))
            Spacer(Modifier.height(10.dp))
        }
        if (status.isNotEmpty()) {
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                .background(XyColor.card).padding(14.dp)) {
                Text(status, color=XyColor.txt1, fontSize=12.sp)
            }
        }
        Spacer(Modifier.height(40.dp))
    }
}
@Composable
private fun XySliderRow(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onChange: (Float) -> Unit,
    onDone: () -> Unit
) {
    Column(Modifier.fillMaxWidth().padding(vertical=6.dp)) {
        Row(verticalAlignment=Alignment.CenterVertically) {
            Text(label, color=XyColor.txt1, fontSize=13.sp,
                fontWeight=FontWeight.Medium, modifier=Modifier.weight(1f))
            Text("%.0f".format(value), color=XyColor.brand,
                fontSize=12.sp, fontWeight=FontWeight.SemiBold)
        }
        Slider(value=value, onValueChange=onChange, onValueChangeFinished=onDone,
            valueRange=range,
            colors=SliderDefaults.colors(thumbColor=XyColor.brand,
                activeTrackColor=XyColor.brand,
                inactiveTrackColor=Color(0xFF2A3038)))
    }
}
