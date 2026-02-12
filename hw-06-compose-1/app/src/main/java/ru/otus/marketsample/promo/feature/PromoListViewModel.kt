package ru.otus.marketsample.promo.feature

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.otus.marketsample.R
import ru.otus.marketsample.promo.domain.ConsumePromosUseCase

class PromoListViewModel(
    private val promoStateFactory: PromoStateFactory,
    private val consumePromosUseCase: ConsumePromosUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(PromoScreenState())
    val state: StateFlow<PromoScreenState> = _state.asStateFlow()

    init {
        requestPromos()
    }

    private fun requestPromos() {
        consumePromosUseCase()
            .map { promos ->
                promos.map(promoStateFactory::map)
            }
            .onStart {
                _state.update { screenState -> screenState.copy(isLoading = true) }
            }
            .onEach { promoListState ->
                _state.update { screenState ->
                    screenState.copy(
                        isLoading = false,
                        promoListState = promoListState,
                        isRefreshing = false
                    )
                }
            }
            .catch {
                _state.update { screenState ->
                    screenState.copy(
                        hasError = true,
                        errorProvider = { context -> context.getString(R.string.error_while_loading_data) }
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true) }
            delay(1000)
            requestPromos()
        }
    }

    fun errorHasShown() {
        _state.update { screenState -> screenState.copy(hasError = false) }
    }
}
