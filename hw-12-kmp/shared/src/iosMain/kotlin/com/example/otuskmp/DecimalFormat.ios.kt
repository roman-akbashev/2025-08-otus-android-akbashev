package com.example.otuskmp

import platform.Foundation.NSDecimalNumber
import platform.Foundation.NSNumberFormatter

actual class DecimalFormat actual constructor(val pattern: String) {
    // fixme: format with pattern
    actual fun format(value: Double): String {
        val formatter = NSNumberFormatter().apply {
            minimumFractionDigits = 2u
            maximumFractionDigits = 2u
        }
        return formatter.stringFromNumber(NSDecimalNumber(value))!!
    }
}