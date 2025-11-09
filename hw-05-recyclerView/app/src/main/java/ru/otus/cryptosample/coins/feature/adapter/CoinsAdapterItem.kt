package ru.otus.cryptosample.coins.feature.adapter

import ru.otus.cryptosample.coins.feature.CoinCategoryState
import ru.otus.cryptosample.coins.feature.CoinState

sealed class CoinsAdapterItem {
    data class CategoryHeader(val categoryName: String) : CoinsAdapterItem()
    data class CoinItem(val coin: CoinState) : CoinsAdapterItem()
    data class CategoryWithHorizontal(val categoryState: CoinCategoryState) : CoinsAdapterItem()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CoinsAdapterItem

        return when (this) {
            is CategoryHeader if other is CategoryHeader -> this.categoryName == other.categoryName
            is CoinItem if other is CoinItem -> this.coin.id == other.coin.id
            is CategoryWithHorizontal if other is CategoryWithHorizontal -> this.categoryState.id == other.categoryState.id
            else -> false
        }
    }

    override fun hashCode(): Int {
        return when (this) {
            is CategoryHeader -> categoryName.hashCode()
            is CoinItem -> coin.id.hashCode()
            is CategoryWithHorizontal -> categoryState.id.hashCode()
        }
    }
}