package ru.otus.marketsample.navigation

import kotlinx.serialization.Serializable

sealed interface Route {
    @Serializable
    data object Products : Route

    @Serializable
    data object Promo : Route
}

sealed interface ProductsRoute : Route {
    @Serializable
    data object List : ProductsRoute

    @Serializable
    data class Details(val id: String) : ProductsRoute
}