package me.weishu.kernelsu.ui.xy
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.MutableSharedFlow
import java.io.File

object XyUnlockBus {
    private val _e = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val event = _e
    fun trigger() { _e.tryEmit(Unit) }
}

object XyStealth {
    private const val PREF = "xy_stealth_v1"
    private const val DEFAULT = "*#*#70707#*#*"
    private const val DIR = "/data/adb/sevenk"
    private const val FILE = "$DIR/stealth_code"

    fun enabled(c: Context): Boolean =
        c.getSharedPreferences(PREF, Context.MODE_PRIVATE).getBoolean("on", false)

    fun code(c: Context): String {
        val l = c.getSharedPreferences(PREF, Context.MODE_PRIVATE).getString("code", null)
        if (l != null) return l
        return try {
            val f = File(FILE)
            if (f.exists()) f.readText().trim().ifEmpty { DEFAULT } else DEFAULT
        } catch (_: Exception) { DEFAULT }
    }

    fun setEnabled(c: Context, on: Boolean) {
        c.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit()
            .putBoolean("on", on).apply()
    }

    fun setCode(c: Context, code: String) {
        c.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit()
            .putString("code", code).apply()
        try {
            val d = File(DIR)
            if (!d.exists()) d.mkdirs()
            File(FILE).writeText(code)
        } catch (_: Exception) {}
    }
}

@Composable
fun XyFakeNotInstalled(onSecret: (String) -> Unit) {
    var showInput by remember { mutableStateOf(false) }
    var input by remember { mutableStateOf("") }
    Box(
        Modifier.fillMaxSize().background(Color(0xFF0B0B0D)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Box(
                Modifier.size(80.dp).clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF17181C)),
                contentAlignment = Alignment.Center
            ) {
                Text("!", color = Color(0xFF6C6F7E), fontSize = 44.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(24.dp))
            Text("未安装", color = Color(0xFFA9ABB6), fontSize = 22.sp, fontWeight = FontWeight.W600)
            Spacer(Modifier.height(8.dp))
            Text("KernelSU 未安装", color = Color(0xFF6C6F7E), fontSize = 13.sp)
            Spacer(Modifier.height(40.dp))
            Text("提示：拨号盘输入 *#*#70707#*#* 解锁",
                color = Color(0xFF4B4D58), fontSize = 11.sp)
            Spacer(Modifier.height(12.dp))
            Text(if (showInput) "也可在此输入" else "或在应用内输入",
                color = Color(0xFF4B4D58), fontSize = 10.sp,
                modifier = Modifier.clickable { showInput = !showInput })
            if (showInput) {
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it; onSecret(it) },
                    placeholder = { Text("*#*#", color = Color(0xFF4B4D58)) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF774F6F),
                        unfocusedBorderColor = Color(0xFF2E2F37),
                        focusedTextColor = Color(0xFFE3E4E8),
                        unfocusedTextColor = Color(0xFFE3E4E8)
                    ),
                    modifier = Modifier.width(260.dp)
                )
            }
        }
    }
}

@Composable
fun XyStealthGate(content: @Composable () -> Unit) {
    val ctx = LocalContext.current
    val on = remember { XyStealth.enabled(ctx) }
    var unlocked by remember { mutableStateOf(!on) }
    LaunchedEffect(Unit) {
        XyUnlockBus.event.collect { unlocked = true }
    }
    if (unlocked) content() else XyFakeNotInstalled { code ->
        if (code == XyStealth.code(ctx)) unlocked = true
    }
}
