package ru.otus.cryptosample.coins.feature.adapter

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.otus.cryptosample.coins.feature.CoinCategoryState
import ru.otus.cryptosample.databinding.ItemCategoryWithHorizontalBinding

class CategoryWithHorizontalViewHolder(
    private val binding: ItemCategoryWithHorizontalBinding,
    private val sharedViewPool: RecyclerView.RecycledViewPool
) : RecyclerView.ViewHolder(binding.root) {

    private val horizontalAdapter = HorizontalCoinsAdapter()

    init {
        binding.horizontalRecyclerView.apply {
            layoutManager = LinearLayoutManager(binding.root.context, LinearLayoutManager.HORIZONTAL, false)
            adapter = horizontalAdapter
            setRecycledViewPool(sharedViewPool)
        }
    }

    fun bind(categoryState: CoinCategoryState) {
        binding.categoryTitle.text = categoryState.name
        horizontalAdapter.submitList(categoryState.coins)
    }
}