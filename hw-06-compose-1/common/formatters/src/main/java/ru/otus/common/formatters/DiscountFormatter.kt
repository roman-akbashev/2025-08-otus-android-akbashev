package ru.otus.common.formatters

import javax.inject.Inject

class DiscountFormatter @Inject constructor() {
    fun format(discount: Int): String {
        return String.format("%d %%", discount)
    }
}