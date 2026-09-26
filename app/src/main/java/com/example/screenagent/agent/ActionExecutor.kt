package com.example.screenagent.agent
import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Bundle
import android.view.accessibility.AccessibilityNodeInfo
class ActionExecutor(private val svc: AccessibilityService) {
    private var nodes: List<UiNode> = emptyList()
    private var refs: List<AccessibilityNodeInfo> = emptyList()
    fun update(ns: List<UiNode>, rs: List<AccessibilityNodeInfo>) {
        nodes = ns; refs = rs
    }
    fun execute(a: AgentAction): Boolean = when (a) {
        is AgentAction.Click -> click(a.index)
        is AgentAction.Input -> input(a.index, a.text)
        is AgentAction.Scroll -> scroll(a.index, a.direction)
        is AgentAction.Back -> svc.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
        is AgentAction.Home -> svc.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME)
        is AgentAction.Wait -> { Thread.sleep(a.ms.toLong()); true }
        is AgentAction.Finish -> true
    }
    private fun click(i: Int): Boolean {
        val n = nodes.getOrNull(i) ?: return false
        val r = refs.getOrNull(i)
        if (r != null && r.isClickable) {
            if (r.performAction(AccessibilityNodeInfo.ACTION_CLICK)) return true
            var p = r.parent
            while (p != null) {
                if (p.isClickable && p.performAction(AccessibilityNodeInfo.ACTION_CLICK)) return true
                p = p.parent
            }
        }
        return gesture(n.bounds.centerX().toFloat(), n.bounds.centerY().toFloat())
    }
    private fun gesture(x: Float, y: Float): Boolean {
        val p = Path().apply { moveTo(x, y) }
        val g = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(p, 0, 60))
            .build()
        return svc.dispatchGesture(g, null, null)
    }
    private fun input(i: Int, t: String): Boolean {
        val r = refs.getOrNull(i) ?: return false
        r.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
        val b = Bundle().apply {
            putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, t)
        }
        return r.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, b)
    }
    private fun scroll(i: Int, d: String): Boolean {
        val r = refs.getOrNull(i) ?: return false
        val a = if (d == "down") AccessibilityNodeInfo.ACTION_SCROLL_FORWARD
                else AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD
        return r.performAction(a)
    }
}
