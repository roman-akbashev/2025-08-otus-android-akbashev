package ru.otus.cryptomvisample.features.coins

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import ru.otus.common.di.FeatureScope
import ru.otus.cryptomvisample.common.domain_api.ConsumeCoinsUseCase
import ru.otus.cryptomvisample.common.domain_api.SetFavouriteCoinUseCase
import ru.otus.cryptomvisample.common.domain_api.UnsetFavouriteCoinUseCase
import javax.inject.Inject

@FeatureScope
class CoinListViewModel @Inject constructor(
    private val consumeCoinsUseCase: ConsumeCoinsUseCase,
    private val coinsStateFactory: CoinsStateFactory,
    private val setFavouriteCoinUseCase: SetFavouriteCoinUseCase,
    private val unsetFavouriteCoinUseCase: UnsetFavouriteCoinUseCase,
) : ViewModel(), ContainerHost<CoinListViewModel.State, CoinListViewModel.SideEffect> {

    override val container = container<State, SideEffect>(State()) {
        loadCoins()
    }

    data class State(
        val categories: List<CoinCategoryState> = emptyList(),
        val highlightMovers: Boolean = false,
        val isLoading: Boolean = false,
        val error: String? = null
    )

    sealed interface SideEffect {
        data class ShowError(val message: String) : SideEffect
        object ToggleFavouriteSuccess : SideEffect
        data class ToggleFavouriteError(val message: String) : SideEffect
    }

    fun loadCoins() = intent {
        consumeCoinsUseCase()
            .onStart {
                reduce { state.copy(isLoading = true, error = null) }
            }
            .map { categories ->
                categories.map { category ->
                    coinsStateFactory.create(category)
                }
            }
            .onEach { categoryListState ->
                val processedCategories = categoryListState.map { category ->
                    category.copy(coins = category.coins.map { coin ->
                        coin.copy(
                            highlight = state.highlightMovers && coin.isHotMover
                        )
                    })
                }

                reduce {
                    state.copy(
                        categories = processedCategories,
                        isLoading = false,
                        error = null
                    )
                }
            }
            .catch { throwable ->
                reduce {
                    state.copy(
                        isLoading = false,
                        error = throwable.message ?: "Failed to load coins"
                    )
                }
                postSideEffect(SideEffect.ShowError(throwable.message ?: "Failed to load coins"))
            }
            .launchIn(viewModelScope)
    }

    fun toggleHighlightMovers(isChecked: Boolean) = intent {
        reduce { state.copy(highlightMovers = isChecked) }

        val processedCategories = state.categories.map { category ->
            category.copy(coins = category.coins.map { coin ->
                coin.copy(highlight = isChecked && coin.isHotMover)
            })
        }

        reduce { state.copy(categories = processedCategories) }
    }

    fun toggleFavourite(coinId: String) = intent {
        viewModelScope.launch {
            val isCurrentlyFavorite = state.categories.any { category ->
                category.coins.any { coin -> coin.id == coinId && coin.isFavourite }
            }

            kotlin.runCatching {
                if (isCurrentlyFavorite) {
                    unsetFavouriteCoinUseCase(coinId)
                } else {
                    setFavouriteCoinUseCase(coinId)
                }
            }.fold(
                onSuccess = {
                    val updatedCategories = state.categories.map { category ->
                        category.copy(coins = category.coins.map { coin ->
                            if (coin.id == coinId) {
                                coin.copy(isFavourite = !isCurrentlyFavorite)
                            } else {
                                coin
                            }
                        })
                    }

                    reduce { state.copy(categories = updatedCategories) }
                    postSideEffect(SideEffect.ToggleFavouriteSuccess)
                },
                onFailure = { throwable ->
                    postSideEffect(
                        SideEffect.ToggleFavouriteError(
                            throwable.message ?: "Failed to toggle favourite"
                        )
                    )
                }
            )
        }
    }
}