package com.example.screenagent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.screenagent.service.ScreenAgentService
class CommandReceiver : BroadcastReceiver() {
    override fun onReceive(c: Context, i: Intent) {
        val s = ScreenAgentService.instance ?: return
        when (i.action) {
            "com.example.screenagent.COMMAND" -> s.loop.submit(i.getStringExtra("goal") ?: return)
            "com.example.screenagent.AUTO_START" -> s.loop.startAuto(i.getStringExtra("goal") ?: "观察屏幕")
            "com.example.screenagent.STOP" -> s.loop.stop()
        }
    }
}
