package com.example.screenagent.agent
import android.graphics.Rect
data class UiNode(
    val index: Int, val className: String,
    val text: String?, val contentDesc: String?, val viewId: String?,
    val bounds: Rect, val clickable: Boolean, val editable: Boolean,
    val scrollable: Boolean, val checkable: Boolean, val checked: Boolean
) {
    fun toPromptLine(): String {
        val sb = StringBuilder("[$index] ")
        sb.append(className.substringAfterLast('.'))
        text?.takeIf { it.isNotBlank() }?.let { sb.append(" text=\"${it.take(40)}\"") }
        contentDesc?.takeIf { it.isNotBlank() }?.let { sb.append(" desc=\"${it.take(30)}\"") }
        viewId?.substringAfterLast('/')?.let { sb.append(" id=$it") }
        val f = buildList {
            if (clickable) add("clickable"); if (editable) add("editable")
            if (scrollable) add("scrollable")
        }
        if (f.isNotEmpty()) sb.append(" [${f.joinToString(",")}]")
        sb.append(" @(${bounds.centerX()},${bounds.centerY()})")
        return sb.toString()
    }
}
