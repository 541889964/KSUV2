package com.example.screenagent.agent
import android.accessibilityservice.AccessibilityService
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import com.example.screenagent.llm.LlmEngine
import com.example.screenagent.llm.PromptBuilder
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
class AgentLoop(private val svc: AccessibilityService, private val llm: LlmEngine) {
    @Volatile var mode: AgentMode = AgentMode.MANUAL; private set
    @Volatile private var autoGoal: String? = null
    @Volatile private var dirty = true
    private val q = LinkedBlockingQueue<String>()
    private val ex = ActionExecutor(svc)
    private val hist = mutableListOf<String>()
    private val lastAt = AtomicLong(0)
    @Volatile private var running = false
    private val ui = Handler(Looper.getMainLooper())
    @Volatile var onState: ((String) -> Unit)? = null
    fun submit(g: String) { q.offer(g); ensure(); emit("命令: $g") }
    fun startAuto(g: String) { autoGoal = g; mode = AgentMode.AUTO; dirty = true; ensure(); emit("自动: $g") }
    fun stop() { mode = AgentMode.MANUAL; autoGoal = null; q.clear(); emit("已停止") }
    fun onScreenEvent() { dirty = true }
    private fun ensure() {
        if (running) return
        running = true
        Thread({ worker() }, "agent").apply { isDaemon = true }.start()
    }
    private fun worker() {
        try {
            while (running) {
                when (mode) {
                    AgentMode.MANUAL -> {
                        val g = q.poll(300, TimeUnit.MILLISECONDS)
                        if (g != null) run(g, 15)
                    }
                    AgentMode.AUTO -> {
                        val g = autoGoal
                        if (g == null) { Thread.sleep(300); continue }
                        val now = SystemClock.elapsedRealtime()
                        if (dirty && now - lastAt.get() >= 2500) {
                            dirty = false
                            run(g, 4)
                            lastAt.set(SystemClock.elapsedRealtime())
                        } else Thread.sleep(300)
                    }
                }
            }
        } catch (_: InterruptedException) {}
        finally { running = false }
    }
    private fun run(goal: String, steps: Int) {
        hist.clear()
        var s = 0
        var last: String? = null
        var rep = 0
        while (running && s < steps) {
            s++
            val root = svc.rootInActiveWindow ?: run { Thread.sleep(500); continue }
            val snap = UiTreeExtractor.extract(root)
            if (snap.nodes.isEmpty()) { Thread.sleep(500); continue }
            ex.update(snap.nodes, snap.refs)
            val prompt = PromptBuilder.build(goal, snap.nodes, hist, ScreenInfo.get(svc))
            val a = try { AgentAction.parse(llm.generate(prompt)) }
                    catch (_: Exception) { Thread.sleep(300); continue }
            hist.add(a.toString())
            if (a is AgentAction.Finish) {
                emit("完成: ${a.result.ifBlank { "无输出" }}")
                return
            }
            val sig = a.toString()
            if (sig == last) { rep++; if (rep >= 3) { emit("重复动作中止"); return } }
            else { last = sig; rep = 0 }
            ex.execute(a)
            Thread.sleep(if (a is AgentAction.Wait) 0 else 800)
        }
        if (mode == AgentMode.MANUAL) emit("达到步数上限")
    }
    private fun emit(m: String) { ui.post { onState?.invoke(m) } }
}
