package ru.otus.cryptosample.coins.feature.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.otus.cryptosample.coins.feature.CoinCategoryState
import ru.otus.cryptosample.databinding.ItemCategoryHeaderBinding
import ru.otus.cryptosample.databinding.ItemCategoryWithHorizontalBinding
import ru.otus.cryptosample.databinding.ItemCoinBinding

class CoinsAdapter(
    private val sharedViewPool: RecyclerView.RecycledViewPool = RecyclerView.RecycledViewPool()
) : ListAdapter<CoinsAdapterItem, RecyclerView.ViewHolder>(CoinsDiffUtil()) {

    companion object {
        private const val VIEW_TYPE_CATEGORY_HEADER = 0
        private const val VIEW_TYPE_COIN = 1
        private const val VIEW_TYPE_CATEGORY_HORIZONTAL = 2
    }

    fun setData(categories: List<CoinCategoryState>) {
        val adapterItems = mutableListOf<CoinsAdapterItem>()

        categories.forEach { category ->
            if (category.coins.size > 10) {
                adapterItems.add(CoinsAdapterItem.CategoryWithHorizontal(category))
            } else {
                adapterItems.add(CoinsAdapterItem.CategoryHeader(category.name))
                category.coins.forEach { coin ->
                    adapterItems.add(CoinsAdapterItem.CoinItem(coin))
                }
            }
        }

        submitList(adapterItems)
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is CoinsAdapterItem.CategoryHeader -> VIEW_TYPE_CATEGORY_HEADER
            is CoinsAdapterItem.CoinItem -> VIEW_TYPE_COIN
            is CoinsAdapterItem.CategoryWithHorizontal -> VIEW_TYPE_CATEGORY_HORIZONTAL
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_CATEGORY_HEADER -> CategoryHeaderViewHolder(
                ItemCategoryHeaderBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            VIEW_TYPE_COIN -> CoinViewHolder(
                ItemCoinBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            VIEW_TYPE_CATEGORY_HORIZONTAL -> CategoryWithHorizontalViewHolder(
                ItemCategoryWithHorizontalBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                ),
                sharedViewPool
            )
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is CoinsAdapterItem.CategoryHeader -> {
                (holder as CategoryHeaderViewHolder).bind(item.categoryName)
            }
            is CoinsAdapterItem.CoinItem -> {
                (holder as CoinViewHolder).bind(item.coin)
            }
            is CoinsAdapterItem.CategoryWithHorizontal -> {
                (holder as CategoryWithHorizontalViewHolder).bind(item.categoryState)
            }
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isNotEmpty()) {
            when (val item = getItem(position)) {
                is CoinsAdapterItem.CoinItem -> {
                    (holder as CoinViewHolder).bindWithPayload(item.coin, payloads)
                }

                else -> super.onBindViewHolder(holder, position, payloads)
            }
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }
}