package me.weishu.kernelsu.ui.xy
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun XyStatusBanner() {
    val message = remember { "希望以后你能开心" }
    Box(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(listOf(
                Color(0xCC1A0A12), Color(0xCC0B0B0D))))
            .border(1.dp, Brush.linearGradient(listOf(
                XyColor.brand.copy(alpha = 0.35f),
                Color.Transparent,
                XyColor.red.copy(alpha = 0.25f))),
                RoundedCornerShape(16.dp))
            .padding(horizontal = 20.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(6.dp).clip(RoundedCornerShape(3.dp))
                .background(Brush.linearGradient(listOf(XyColor.brand, XyColor.red))))
            Spacer(Modifier.width(12.dp))
            Text(
                text = message,
                color = Color(0xFFF5D5DD),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                fontStyle = FontStyle.Italic,
                letterSpacing = 1.sp
            )
        }
    }
}
