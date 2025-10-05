package otus.homework.coroutines

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class DiContainer {

    private val factRetrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://catfact.ninja/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val imageRetrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.thecatapi.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val catsService: CatsService = object : CatsService {
        private val factService = factRetrofit.create(CatsService::class.java)
        private val imageService = imageRetrofit.create(CatsService::class.java)

        override suspend fun getCatFact(): Fact = factService.getCatFact()
        override suspend fun getCatImage(): List<Image> = imageService.getCatImage()
    }
}