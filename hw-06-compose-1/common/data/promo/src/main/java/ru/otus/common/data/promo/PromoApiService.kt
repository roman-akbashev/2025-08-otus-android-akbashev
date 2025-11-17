package ru.otus.common.data.promo

import retrofit2.http.GET

interface PromoApiService {
    @GET("/static/compose-hw1/promo.json")
    suspend fun getPromos(): List<PromoDto>
}
