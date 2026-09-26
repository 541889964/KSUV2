package com.example.screenagent
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.screenagent.agent.ScreenInfo
import com.example.screenagent.service.ScreenAgentService
class MainActivity : AppCompatActivity() {
    private lateinit var tvScr: TextView
    private lateinit var tvSt: TextView
    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        val l = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 80, 40, 40)
        }
        tvScr = TextView(this).apply { textSize = 13f; setPadding(0, 0, 0, 16) }
        tvSt = TextView(this).apply { textSize = 13f; setPadding(0, 16, 0, 0) }
        val btnAcc = Button(this).apply { text = "1. 开启无障碍" }
        btnAcc.setOnClickListener { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
        val input = EditText(this).apply { hint = "输入任务，如：打开 WiFi 设置" }
        val btnSend = Button(this).apply { text = "发送" }
        btnSend.setOnClickListener {
            val s = ScreenAgentService.instance
            if (s == null) {
                Toast.makeText(this, "先开无障碍", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val g = input.text.toString().trim()
            if (g.isEmpty()) return@setOnClickListener
            s.loop.submit(g)
        }
        val btnAuto = Button(this).apply { text = "自动模式" }
        btnAuto.setOnClickListener {
            val s = ScreenAgentService.instance ?: return@setOnClickListener
            s.loop.startAuto(input.text.toString().trim().ifEmpty { "观察屏幕" })
        }
        val btnStop = Button(this).apply { text = "停止" }
        btnStop.setOnClickListener { ScreenAgentService.instance?.loop?.stop() }
        listOf(tvScr, btnAcc, input, btnSend, btnAuto, btnStop, tvSt).forEach { l.addView(it) }
        setContentView(l)
    }
    override fun onResume() {
        super.onResume()
        val i = ScreenInfo.get(this)
        tvScr.text = "屏幕: ${i.width}x${i.height} rot=${i.rotation}"
        ScreenAgentService.instance?.loop?.onState = { m ->
            runOnUiThread { tvSt.text = m }
        }
    }
    override fun onPause() {
        super.onPause()
        ScreenAgentService.instance?.loop?.onState = null
    }
}
