package com.example.otuskmp

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

actual class ClipboardManager {
    actual fun copyText(text: String) {
        val context = getApplicationContext()
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Stopwatch Result", text)
        clipboard.setPrimaryClip(clip)
    }

    private fun getApplicationContext(): Context {
        return AndroidContext.applicationContext
    }
}

object AndroidContext {
    lateinit var applicationContext: Context
        private set

    fun init(context: Context) {
        applicationContext = context
    }
}