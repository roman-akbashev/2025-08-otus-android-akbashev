package ru.otus.cryptosample.coins.feature.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import ru.otus.cryptosample.coins.feature.CoinState
import ru.otus.cryptosample.databinding.ItemCoinHorizontalBinding

class HorizontalCoinsAdapter : ListAdapter<CoinState, HorizontalCoinViewHolder>(
    object : DiffUtil.ItemCallback<CoinState>() {
        override fun areItemsTheSame(oldItem: CoinState, newItem: CoinState): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CoinState, newItem: CoinState): Boolean {
            return oldItem == newItem
        }

        override fun getChangePayload(oldItem: CoinState, newItem: CoinState): Any? {
            if (oldItem.highlight != newItem.highlight) {
                return CoinsDiffUtil.PAYLOAD_HIGHLIGHT_CHANGED
            }
            return null
        }
    }
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HorizontalCoinViewHolder {
        return HorizontalCoinViewHolder(
            ItemCoinHorizontalBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: HorizontalCoinViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onBindViewHolder(
        holder: HorizontalCoinViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isNotEmpty()) {
            holder.bindWithPayload(getItem(position), payloads)
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }
}