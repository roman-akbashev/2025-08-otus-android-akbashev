package ru.otus.common.data.promo

import com.google.gson.annotations.SerializedName

data class PromoDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("image") val image: String,
    @SerializedName("discount") val discount: Double,
    @SerializedName("description") val description: String,
    @SerializedName("type") val type: String,
    @SerializedName("products") val products: List<String>?,
)