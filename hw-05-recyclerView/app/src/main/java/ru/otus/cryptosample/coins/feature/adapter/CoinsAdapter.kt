package ru.otus.cryptosample.coins.feature.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.otus.cryptosample.coins.feature.CoinCategoryState
import ru.otus.cryptosample.databinding.ItemCategoryHeaderBinding
import ru.otus.cryptosample.databinding.ItemCoinBinding

class CoinsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    
    companion object {
        private const val VIEW_TYPE_CATEGORY = 0
        private const val VIEW_TYPE_COIN = 1
    }
    
    private var items = listOf<CoinsAdapterItem>()
    
    fun setData(categories: List<CoinCategoryState>) {
        val adapterItems = mutableListOf<CoinsAdapterItem>()
        
        categories.forEach { category ->
            adapterItems.add(CoinsAdapterItem.CategoryHeader(category.name))
            category.coins.forEach { coin ->
                adapterItems.add(CoinsAdapterItem.CoinItem(coin))
            }
        }
        
        items = adapterItems
        notifyDataSetChanged()
    }
    
    override fun getItemCount(): Int = items.size
    
    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is CoinsAdapterItem.CategoryHeader -> VIEW_TYPE_CATEGORY
            is CoinsAdapterItem.CoinItem -> VIEW_TYPE_COIN
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_CATEGORY -> CategoryHeaderViewHolder(
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
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }
    
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is CoinsAdapterItem.CategoryHeader -> {
                (holder as CategoryHeaderViewHolder).bind(item.categoryName)
            }
            is CoinsAdapterItem.CoinItem -> {
                (holder as CoinViewHolder).bind(item.coin)
            }
        }
    }
}