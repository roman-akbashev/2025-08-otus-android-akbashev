package ru.otus.marketsample.promo.domain

sealed class Promo(
    open val id: String,
    val name: String,
    val image: String,
    val description: String,
    val discount: Double,
) {

    class PromoForProducts(
        id: String,
        name: String,
        image: String,
        description: String,
        discount: Double,
        val products: List<String>,
    ): Promo(
        id = id,
        name = name,
        image = image,
        description = description,
        discount = discount,
    )

    class PromoForPrice(
        id: String,
        name: String,
        image: String,
        description: String,
        discount: Double,
    ): Promo(
        id = id,
        name = name,
        image = image,
        description = description,
        discount = discount,
    )
}
