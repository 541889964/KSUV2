package com.example.screenagent.agent
import android.graphics.Rect
import android.view.accessibility.AccessibilityNodeInfo
data class UiSnapshot(val nodes: List<UiNode>, val refs: List<AccessibilityNodeInfo>)
object UiTreeExtractor {
    private const val MAX = 80
    fun extract(root: AccessibilityNodeInfo?): UiSnapshot {
        val ns = ArrayList<UiNode>(MAX)
        val rs = ArrayList<AccessibilityNodeInfo>(MAX)
        if (root == null) return UiSnapshot(ns, rs)
        walk(root, ns, rs, intArrayOf(0))
        return UiSnapshot(ns, rs)
    }
    private fun walk(n: AccessibilityNodeInfo?, ns: MutableList<UiNode>, rs: MutableList<AccessibilityNodeInfo>, c: IntArray) {
        if (n == null || c[0] >= MAX) return
        val t = n.text?.toString()
        val d = n.contentDescription?.toString()
        val ok = n.isClickable || n.isEditable || n.isScrollable || !t.isNullOrBlank() || !d.isNullOrBlank()
        if (ok) {
            val r = Rect()
            n.getBoundsInScreen(r)
            if (r.width() > 0 && r.height() > 0) {
                val i = c[0]++
                ns.add(UiNode(i, n.className?.toString() ?: "", t, d, n.viewIdResourceName, r,
                    n.isClickable, n.isEditable, n.isScrollable, n.isCheckable, n.isChecked))
                rs.add(n)
            }
        }
        for (i in 0 until n.childCount) walk(n.getChild(i), ns, rs, c)
    }
}
