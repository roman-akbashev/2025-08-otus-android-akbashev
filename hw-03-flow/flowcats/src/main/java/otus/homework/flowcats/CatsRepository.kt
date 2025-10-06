package otus.homework.flowcats

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class CatsRepository(
    private val catsService: CatsService,
    private val refreshIntervalMs: Long = 5000
) {

    fun listenForCatFacts() = flow {
        while (true) {
            val fact = try {
                Result.Success(catsService.getCatFact())
            } catch (e: Exception) {
                Result.Error(e.message ?: "Unknown error")
            }
            emit(fact)
            delay(refreshIntervalMs)
        }
    }.flowOn(Dispatchers.IO)
}