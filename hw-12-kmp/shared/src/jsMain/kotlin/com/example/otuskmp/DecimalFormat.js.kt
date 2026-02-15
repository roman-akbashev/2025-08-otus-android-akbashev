package com.example.otuskmp

actual class DecimalFormat actual constructor(val pattern: String) {
    actual fun format(value: Double): String {
        val rounded = kotlin.math.round(value * 100) / 100
        val parts = rounded.toString().split('.')
        return if (parts.size == 1) {
            "${parts[0]}.00"
        } else {
            parts[0] + "." + parts[1].padEnd(2, '0')
        }
    }
}