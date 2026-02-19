package com.example.otuskmp

import platform.UIKit.UIPasteboard

actual class ClipboardManager {
    actual fun copyText(text: String) {
        UIPasteboard.generalPasteboard.string = text
    }
}