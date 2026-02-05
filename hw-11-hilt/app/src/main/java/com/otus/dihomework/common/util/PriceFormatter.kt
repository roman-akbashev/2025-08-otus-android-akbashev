package com.otus.dihomework.common.util

class PriceFormatter() {

    fun format(price: Double): String {
        return "${price.toInt()} ₽"
    }
}
