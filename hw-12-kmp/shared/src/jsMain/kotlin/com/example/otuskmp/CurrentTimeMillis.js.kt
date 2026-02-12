package com.example.otuskmp

import kotlinx.browser.window

actual fun currentTimeMillis(): Long {
    return window.performance.now().toLong()
}