package ru.otus.cryptosample.coins.domain

import ru.otus.cryptosample.coins.data.CoinEntity
import javax.inject.Inject

class CoinDomainMapper @Inject constructor() {
    fun fromEntity(coinEntity: CoinEntity): Coin {
        return Coin(
            id = coinEntity.id,
            name = coinEntity.name,
            ticker = coinEntity.ticker,
            price = coinEntity.price,
            change24h = coinEntity.change24h,
            iconPath = coinEntity.iconPath
        )
    }
}
