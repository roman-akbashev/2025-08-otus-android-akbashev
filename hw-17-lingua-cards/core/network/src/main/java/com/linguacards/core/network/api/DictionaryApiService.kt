package com.linguacards.core.network.api

import com.linguacards.core.network.dto.WordResponseDto
import retrofit2.http.GET
import retrofit2.http.Path

interface DictionaryApiService {

    @GET("entries/en/{word}")
    suspend fun getWordInfo(@Path("word") word: String): List<WordResponseDto>
}