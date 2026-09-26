package me.weishu.kernelsu.ui.xy
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
@Composable
fun XyGroup(title: String, accent: Color = XyColor.brand, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 6.dp, bottom = 10.dp)) {
            Box(Modifier.size(3.dp, 14.dp).clip(RoundedCornerShape(2.dp))
                .background(Brush.verticalGradient(listOf(accent, accent.copy(alpha = 0.4f)))))
            Spacer(Modifier.width(10.dp))
            Text(title, color = XyColor.txt2, fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold, letterSpacing = 1.2.sp)
        }
        Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFF1A1B20), Color(0xFF141519))))
            .border(1.dp, Brush.linearGradient(listOf(
                accent.copy(alpha = 0.18f), Color.Transparent,
                accent.copy(alpha = 0.08f))), RoundedCornerShape(16.dp))
            .padding(14.dp)) { Column(content = content) }
    }
}
@Composable
fun XyChip(label: String, sel: Boolean, accent: Color = XyColor.brand, onClick: () -> Unit) {
    val bg = if (sel) Brush.linearGradient(listOf(accent.copy(alpha = 0.45f), accent.copy(alpha = 0.28f)))
             else Brush.linearGradient(listOf(Color(0xFF232831), Color(0xFF1C2028)))
    Box(Modifier.clip(RoundedCornerShape(10.dp)).background(bg)
        .then(if (sel) Modifier.border(1.dp,
            Brush.linearGradient(listOf(accent.copy(alpha = 0.6f), accent.copy(alpha = 0.25f))),
            RoundedCornerShape(10.dp)) else Modifier)
        .hapticClick { onClick() }
        .padding(horizontal = 12.dp, vertical = 7.dp)) {
        Text(label, color = if (sel) Color.White else XyColor.txt2,
            fontSize = 11.sp, fontWeight = if (sel) FontWeight.SemiBold else FontWeight.Normal)
    }
}
@Composable
fun XyPrimaryBtn(label: String, onClick: () -> Unit) {
    Box(Modifier.fillMaxWidth().height(52.dp).clip(RoundedCornerShape(14.dp))
        .background(Brush.horizontalGradient(listOf(XyColor.red, XyColor.red2, XyColor.red3)))
        .hapticClick(scaleDown = 0.97f) { onClick() },
        contentAlignment = Alignment.Center) {
        Text(label, color = Color.White, fontSize = 14.sp,
            fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
    }
}
