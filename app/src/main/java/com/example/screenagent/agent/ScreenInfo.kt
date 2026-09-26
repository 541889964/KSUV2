package com.example.screenagent.agent
import android.content.Context
import android.graphics.Point
import android.os.Build
import android.view.WindowManager
object ScreenInfo {
    data class Info(val width: Int, val height: Int, val rotation: Int, val density: Float)
    fun get(ctx: Context): Info {
        val wm = ctx.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val dm = ctx.resources.displayMetrics
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val b = wm.currentWindowMetrics.bounds
            Info(b.width(), b.height(), ctx.display?.rotation ?: 0, dm.density)
        } else {
            val p = Point()
            @Suppress("DEPRECATION") wm.defaultDisplay.getRealSize(p)
            @Suppress("DEPRECATION") Info(p.x, p.y, wm.defaultDisplay.rotation, dm.density)
        }
    }
}
