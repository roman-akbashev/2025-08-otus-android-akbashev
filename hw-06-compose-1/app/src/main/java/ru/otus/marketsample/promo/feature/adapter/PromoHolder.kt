package ru.otus.marketsample.promo.feature.adapter

import androidx.recyclerview.widget.RecyclerView
import coil.load
import ru.otus.marketsample.databinding.ItemPromoBinding
import ru.otus.marketsample.promo.feature.PromoState

class PromoHolder(
    private val binding: ItemPromoBinding,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(promoState: PromoState) {
        binding.image.load(promoState.image)
        binding.name.text = promoState.name
        binding.description.text = promoState.description
    }
}
