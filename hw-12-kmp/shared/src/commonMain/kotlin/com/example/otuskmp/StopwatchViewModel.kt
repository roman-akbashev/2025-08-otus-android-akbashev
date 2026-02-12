package com.example.otuskmp

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

class StopwatchViewModel {
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var job: Job? = null

    private val _uiState = MutableStateFlow(
        StopwatchUiState(
            currentTimeMillis = 0,
            initialTimeMillis = 0
        )
    )
    val uiState: AnyStateFlow<StopwatchUiState> = _uiState.wrapToAny()

    fun onStartClicked() {
        startTimer()
    }

    fun onStopClicked() {
        job?.cancel()
    }

    fun onDestroy() {
        coroutineScope.cancel()
    }

    private fun startTimer() {
        if (job?.isActive == true) return

        _uiState.update {
            val currentTime = currentTimeMillis()
            it.copy(
                currentTimeMillis = currentTime,
                initialTimeMillis = currentTime,
            )
        }

        job = tickerFlow()
            .onEach {
                val currentTime = currentTimeMillis()
                _uiState.update { it.copy(currentTimeMillis = currentTime) }
            }
            .launchIn(coroutineScope)
    }

    private fun tickerFlow(): Flow<Unit> {
        return flow {
            while (true) {
                emit(Unit)
                delay(100)
            }
        }
    }
}

data class StopwatchUiState(
    val currentTimeMillis: Long,
    val initialTimeMillis: Long
) {
    private val formatter = DecimalFormat("0.00")
    val formattedTime: String =
        formatter.format((currentTimeMillis.toDouble() - initialTimeMillis.toDouble()) / 1000.0)
}