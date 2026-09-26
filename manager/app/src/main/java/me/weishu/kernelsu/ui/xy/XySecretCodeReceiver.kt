package me.weishu.kernelsu.ui.xy
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import me.weishu.kernelsu.ui.MainActivity
class XySecretCodeReceiver : BroadcastReceiver() {
    override fun onReceive(context:Context, intent:Intent) {
        if (intent.action != "android.provider.Telephony.SECRET_CODE") return
        XyUnlockBus.trigger()
        try {
            context.startActivity(Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            })
        } catch (_: Exception) {}
    }
}
