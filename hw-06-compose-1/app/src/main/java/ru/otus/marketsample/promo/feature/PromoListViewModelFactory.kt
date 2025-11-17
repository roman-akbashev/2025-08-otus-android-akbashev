package ru.otus.marketsample.promo.feature

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import ru.otus.common.di.FeatureScope
import ru.otus.marketsample.promo.domain.ConsumePromosUseCase
import javax.inject.Inject

@FeatureScope
class PromoListViewModelFactory @Inject constructor(
    private val promoStateFactory: PromoStateFactory,
    private val consumePromosUseCase: ConsumePromosUseCase,
) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>,
        extras: CreationExtras,
    ): T {
        when {
            modelClass.isAssignableFrom(PromoListViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                return PromoListViewModel(
                    promoStateFactory = promoStateFactory,
                    consumePromosUseCase = consumePromosUseCase,
                ) as T
            }
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}