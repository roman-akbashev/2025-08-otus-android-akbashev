package ru.otus.cryptosample.coins.feature.adapter

import androidx.recyclerview.widget.DiffUtil

class CoinsDiffUtil : DiffUtil.ItemCallback<CoinsAdapterItem>() {

    companion object {
        const val PAYLOAD_HIGHLIGHT_CHANGED = "payload_highlight_changed"
    }

    override fun areItemsTheSame(oldItem: CoinsAdapterItem, newItem: CoinsAdapterItem): Boolean {
        return when (oldItem) {
            is CoinsAdapterItem.CategoryHeader if newItem is CoinsAdapterItem.CategoryHeader ->
                oldItem.categoryName == newItem.categoryName

            is CoinsAdapterItem.CoinItem if newItem is CoinsAdapterItem.CoinItem ->
                oldItem.coin.id == newItem.coin.id

            is CoinsAdapterItem.CategoryWithHorizontal if newItem is CoinsAdapterItem.CategoryWithHorizontal ->
                oldItem.categoryState.id == newItem.categoryState.id

            else -> false
        }
    }

    override fun areContentsTheSame(oldItem: CoinsAdapterItem, newItem: CoinsAdapterItem): Boolean {
        return when (oldItem) {
            is CoinsAdapterItem.CategoryHeader if newItem is CoinsAdapterItem.CategoryHeader ->
                oldItem == newItem

            is CoinsAdapterItem.CoinItem if newItem is CoinsAdapterItem.CoinItem ->
                oldItem.coin == newItem.coin

            is CoinsAdapterItem.CategoryWithHorizontal if newItem is CoinsAdapterItem.CategoryWithHorizontal ->
                oldItem.categoryState == newItem.categoryState

            else -> false
        }
    }

    override fun getChangePayload(oldItem: CoinsAdapterItem, newItem: CoinsAdapterItem): Any? {
        return when {
            oldItem is CoinsAdapterItem.CoinItem && newItem is CoinsAdapterItem.CoinItem -> {
                if (oldItem.coin.highlight != newItem.coin.highlight) {
                    return PAYLOAD_HIGHLIGHT_CHANGED
                }
                null
            }
            else -> null
        }
    }
}