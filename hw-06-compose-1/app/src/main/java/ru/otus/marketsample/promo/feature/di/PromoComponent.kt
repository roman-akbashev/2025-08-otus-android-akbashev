package ru.otus.marketsample.promo.feature.di

import dagger.Component
import ru.otus.common.data.promo.PromoRepository
import ru.otus.common.di.FeatureScope
import ru.otus.marketsample.promo.feature.PromoListFragment
import ru.otus.marketsample.promo.feature.PromoListViewModelFactory

@FeatureScope
@Component(dependencies = [PromoComponentDependencies::class])
interface PromoComponent {

    @Component.Factory
    interface Factory {
        fun create(dependencies: PromoComponentDependencies): PromoComponent
    }

    fun inject(productFragment: PromoListFragment)

    fun getPromoViewModelFactory(): PromoListViewModelFactory
}

interface PromoComponentDependencies {
    fun getPromoRepository(): PromoRepository
}
