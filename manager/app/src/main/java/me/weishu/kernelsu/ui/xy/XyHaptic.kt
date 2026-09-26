package me.weishu.kernelsu.ui.xy
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
fun Modifier.hapticClick(
    scaleDown: Float = 0.96f, haptic: Boolean = true,
    enabled: Boolean = true, onClick: () -> Unit
): Modifier = composed {
    val h = LocalHapticFeedback.current
    val i = remember { MutableInteractionSource() }
    val p by i.collectIsPressedAsState()
    val s by animateFloatAsState(
        targetValue = if (p && enabled) scaleDown else 1f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 800f), label = "hc")
    this.scale(s).clickable(
        interactionSource = i, indication = null, enabled = enabled
    ) {
        if (haptic) h.performHapticFeedback(HapticFeedbackType.LongPress)
        onClick()
    }
}
