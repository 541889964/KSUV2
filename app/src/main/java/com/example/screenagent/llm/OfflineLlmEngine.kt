package com.example.screenagent.llm
import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileOutputStream
class OfflineLlmEngine(private val ctx: Context) : LlmEngine {
    private val f: File get() = File(ctx.filesDir, "model.gguf")
    private var ptr: Long = 0
    override fun load() {
        try {
            if (!f.exists()) {
                Log.i("OfflineLlmEngine", "copying model from assets...")
                ctx.assets.open("models/model.gguf").use { i ->
                    FileOutputStream(f).use { o -> i.copyTo(o, 1 shl 20) }
                }
            }
            Log.i("OfflineLlmEngine", "loading model ${f.length()} bytes")
            ptr = LlamaBridge.initContext(f.absolutePath)
            Log.i("OfflineLlmEngine", "loaded ptr=$ptr")
        } catch (e: Exception) {
            Log.e("OfflineLlmEngine", "load failed", e)
        }
    }
    override fun generate(prompt: String, maxTokens: Int): String {
        if (ptr == 0L) return "{\"action\":\"wait\",\"ms\":500}"
        return try {
            LlamaBridge.generate(ptr, prompt, maxTokens)
        } catch (e: Exception) {
            "{\"action\":\"wait\",\"ms\":500}"
        }
    }
    override fun release() {
        if (ptr != 0L) { LlamaBridge.freeContext(ptr); ptr = 0 }
    }
}
