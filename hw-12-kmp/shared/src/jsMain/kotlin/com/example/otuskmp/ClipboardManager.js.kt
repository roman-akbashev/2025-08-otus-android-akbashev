package com.example.otuskmp

import kotlinx.browser.window

actual class ClipboardManager {
    actual fun copyText(text: String) {
        window.navigator.clipboard.writeText(text)
    }
}