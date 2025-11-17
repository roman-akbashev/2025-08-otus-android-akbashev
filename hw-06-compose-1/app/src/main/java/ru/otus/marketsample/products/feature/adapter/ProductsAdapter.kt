package ru.otus.marketsample.products.feature.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import ru.otus.common.di.FeatureScope
import ru.otus.marketsample.databinding.ItemProductBinding
import ru.otus.marketsample.products.feature.ProductState
import javax.inject.Inject

@FeatureScope
class ProductsAdapter @Inject constructor(
    private val onItemClicked: (String) -> Unit,
) :
    ListAdapter<ProductState, ProductHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductHolder {
        return ProductHolder(
            binding = ItemProductBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            ),
            onItemClicked = onItemClicked,
        )
    }

    override fun onBindViewHolder(holder: ProductHolder, position: Int) {
        val entity = getItem(position)
        entity?.let {
            holder.bind(entity)
        }
    }
}

private class DiffCallback : DiffUtil.ItemCallback<ProductState>() {

    override fun areItemsTheSame(oldItem: ProductState, newItem: ProductState): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: ProductState, newItem: ProductState): Boolean {
        return oldItem == newItem
    }
}
