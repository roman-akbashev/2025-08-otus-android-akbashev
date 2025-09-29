package otus.homework.coroutines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.SocketTimeoutException

class CatsViewModel(
    private val catsService: CatsService
) : ViewModel() {

    private val _catState =
        MutableStateFlow<Result>(Result.Error("Не удалось получить данные о кошке"))

    val catState: StateFlow<Result> = _catState.asStateFlow()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        val errorMessage = when (throwable) {
            is SocketTimeoutException -> "Не удалось получить ответ от сервера"
            is IOException -> "Ошибка сети: проверьте подключение"
            else -> throwable.message ?: "Произошла ошибка"
        }
        CrashMonitor.trackWarning()
        _catState.value = Result.Error(errorMessage)
    }

    fun onInitComplete() {
        viewModelScope.launch(exceptionHandler) {
            val factDeferred = async { catsService.getCatFact() }
            val imageDeferred = async { catsService.getCatImage() }
            val catData = CatData(factDeferred.await().fact, imageDeferred.await().first().url)
            _catState.value = Result.Success(catData)
        }
    }

    class CatsViewModelFactory(
        private val catsService: CatsService,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(CatsViewModel::class.java))
            return CatsViewModel(catsService) as T
        }
    }

}