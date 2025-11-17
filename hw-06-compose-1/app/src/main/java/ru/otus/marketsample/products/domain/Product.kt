package ru.otus.marketsample.products.domain

data class Product (
    val id: String,
    val name: String,
    val image: String,
    val price: Double,
    val hasDiscount: Boolean,
    val discount: Double?,
)
