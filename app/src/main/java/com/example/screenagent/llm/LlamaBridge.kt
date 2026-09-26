package com.example.screenagent.llm
object LlamaBridge {
    init { System.loadLibrary("llama-jni") }
    external fun initContext(modelPath: String): Long
    external fun generate(ctxPtr: Long, prompt: String, maxTokens: Int): String
    external fun freeContext(ctxPtr: Long)
}
