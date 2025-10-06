package ru.otus.cryptosample.coins.domain

data class Coin (
    val id: String,
    val name: String,
    val ticker: String,
    val price: Double,
    val change24h: Double,
    val iconPath: String
)

data class CoinCategory (
    val id: String,
    val name: String,
    val coins: List<Coin>
)
