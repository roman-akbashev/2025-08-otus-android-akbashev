package com.example.otuskmp

expect class DecimalFormat(pattern: String) {
    fun format(value: Double): String
}