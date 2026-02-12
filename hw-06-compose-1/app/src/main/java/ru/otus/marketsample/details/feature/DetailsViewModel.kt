package ru.otus.marketsample.details.feature

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import ru.otus.marketsample.details.domain.ConsumeProductDetailsUseCase
import ru.otus.marketsample.R

class DetailsViewModel(
    private val consumeProductDetailsUseCase: ConsumeProductDetailsUseCase,
    private val detailsStateFactory: DetailsStateFactory,
    private val productId: String,
) : ViewModel() {

    private val _state = MutableStateFlow(DetailsScreenState())
    val state: StateFlow<DetailsScreenState> = _state.asStateFlow()

    init {
        requestProducts()
    }

    private fun requestProducts() {
        consumeProductDetailsUseCase(productId)
            .map { productDetails -> detailsStateFactory.create(productDetails) }
            .flowOn(Dispatchers.IO)
            .onStart {
                _state.update { screenState -> screenState.copy(isLoading = true) }
            }
            .onEach { detailsState ->
                _state.update { screenState ->
                    screenState.copy(
                        isLoading = false,
                        detailsState = detailsState,
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

    fun errorHasShown() {
        _state.update { screenState -> screenState.copy(hasError = false) }
    }
}
