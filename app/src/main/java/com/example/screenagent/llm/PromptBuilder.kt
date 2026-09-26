package com.example.screenagent.llm
import com.example.screenagent.agent.ScreenInfo
import com.example.screenagent.agent.UiNode
object PromptBuilder {
    private val SYS = """
你是 Android 自动化助手。只输出一个 JSON 对象。
动作: click/input/scroll/back/home/wait/finish
示例: {"action":"click","index":3}
规则:
- index 必须来自下面的元素列表
- 一次只输出一个动作
- 任务完成时用 {"action":"finish","result":"..."}
""".trimIndent()
    fun build(goal: String, nodes: List<UiNode>, hist: List<String>, sc: ScreenInfo.Info): String {
        val e = nodes.joinToString("\n") { it.toPromptLine() }
        val h = if (hist.isEmpty()) "无" else hist.takeLast(3).joinToString("\n")
        return "$SYS\n\n屏幕: ${sc.width}x${sc.height}\n目标: $goal\n最近动作: $h\n元素:\n$e\n\n输出 JSON:"
    }
}
