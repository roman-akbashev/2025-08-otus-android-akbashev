package otus.homework.coroutines

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException

class CatsPresenter(
    private val catsService: CatsService
) {

    private var _catsView: ICatsView? = null

    private val presenterScope = CoroutineScope(
        Dispatchers.Main + SupervisorJob() + CoroutineName("CatsCoroutine")
    )

    fun onInitComplete() {
        presenterScope.launch {
            try {
                val factDeferred = async { catsService.getCatFact() }
                val imageDeferred = async { catsService.getCatImage() }
                val catData = CatData(factDeferred.await().fact, imageDeferred.await().first().url)
                _catsView?.populate(catData)
            } catch (e: Throwable) {
                handleError(e)
            }

        }
    }

    private fun handleError(e: Throwable) {
        when (e) {
            is SocketTimeoutException -> _catsView?.showToast("Не удалось получить ответ от сервера")
            else -> {
                CrashMonitor.trackWarning()
                _catsView?.showToast(e.message ?: "Произошла ошибка")
            }
        }
    }

    fun attachView(catsView: ICatsView) {
        _catsView = catsView
    }

    fun detachView() {
        presenterScope.cancel()
        _catsView = null
    }
}