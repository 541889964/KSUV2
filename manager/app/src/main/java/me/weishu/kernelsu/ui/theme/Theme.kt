package me.weishu.kernelsu.ui.theme
import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import me.weishu.kernelsu.ui.xy.XyPrefs

private val DarkScheme = darkColorScheme(
    primary = XyPrimer.brand, onPrimary = XyPrimer.gray000,
    primaryContainer = XyPrimer.brandDark, onPrimaryContainer = XyPrimer.gray200,
    secondary = XyPrimer.gray350, onSecondary = XyPrimer.gray950,
    background = XyPrimer.gray950, onBackground = XyPrimer.gray200,
    surface = XyPrimer.gray900, onSurface = XyPrimer.gray200,
    surfaceVariant = XyPrimer.gray850, onSurfaceVariant = XyPrimer.gray350,
    surfaceContainerLowest = XyPrimer.gray1000,
    surfaceContainerLow = XyPrimer.gray950,
    surfaceContainer = XyPrimer.gray900,
    surfaceContainerHigh = XyPrimer.gray850,
    surfaceContainerHighest = XyPrimer.gray800,
    outline = XyPrimer.gray800, outlineVariant = XyPrimer.gray750,
    error = XyPrimer.red400, onError = XyPrimer.gray000
)
private val LightScheme = lightColorScheme(
    primary = XyPrimer.brand, onPrimary = XyPrimer.gray000,
    primaryContainer = XyPrimer.red100, onPrimaryContainer = XyPrimer.red900,
    background = XyPrimer.gray050, onBackground = XyPrimer.gray900,
    surface = XyPrimer.gray000, onSurface = XyPrimer.gray900,
    surfaceVariant = XyPrimer.gray100, onSurfaceVariant = XyPrimer.gray700,
    surfaceContainerLowest = XyPrimer.gray000,
    surfaceContainerLow = XyPrimer.gray050,
    surfaceContainer = XyPrimer.gray100,
    surfaceContainerHigh = XyPrimer.gray150,
    surfaceContainerHighest = XyPrimer.gray200,
    outline = XyPrimer.gray300, outlineVariant = XyPrimer.gray200,
    error = XyPrimer.red900, onError = XyPrimer.gray000
)

@Composable
fun KernelSUTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    appSettings: me.weishu.kernelsu.ui.util.AppSettings? = null,
    uiMode: Any? = null,
    content: @Composable () -> Unit
) {
    val isDark = when (XyPrefs.config.themeMode.id) {
        "light" -> false
        "dark", "amoled" -> true
        else -> darkTheme
    }
    val cs = if (isDark) DarkScheme else LightScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            try {
                val w = (view.context as Activity).window
                w.statusBarColor = Color.Transparent.toArgb()
                w.navigationBarColor = Color.Transparent.toArgb()
                WindowCompat.setDecorFitsSystemWindows(w, false)
                val c = WindowCompat.getInsetsController(w, view)
                c.isAppearanceLightStatusBars = !isDark
                c.isAppearanceLightNavigationBars = !isDark
            } catch (_: Throwable) {}
        }
    }
    MaterialTheme(colorScheme = cs, typography = XyTypography, shapes = XyShapes, content = content)
}
