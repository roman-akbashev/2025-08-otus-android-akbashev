package ru.otus.cryptomvisample.features.favourites

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
import ru.otus.cryptomvisample.common.domain_api.ConsumeFavoriteCoinsUseCase
import ru.otus.cryptomvisample.common.domain_api.UnsetFavouriteCoinUseCase
import javax.inject.Inject

@FeatureScope
class FavoriteViewModel @Inject constructor(
    private val consumeFavoriteCoinsUseCase: ConsumeFavoriteCoinsUseCase,
    private val mapper: FavoriteStateMapper,
    private val unsetFavouriteCoinUseCase: UnsetFavouriteCoinUseCase,
) : ViewModel(), ContainerHost<FavoriteViewModel.State, FavoriteViewModel.SideEffect> {

    override val container = container<State, SideEffect>(State()) {
        loadFavorites()
    }

    data class State(
        val favoriteCoins: List<FavouriteCoinState> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null
    )

    sealed interface SideEffect {
        data class ShowError(val message: String) : SideEffect
        object RemoveFavouriteSuccess : SideEffect
        data class RemoveFavouriteError(val message: String) : SideEffect
    }

    fun loadFavorites() = intent {
        consumeFavoriteCoinsUseCase()
            .onStart {
                reduce { state.copy(isLoading = true, error = null) }
            }
            .map { favoriteCoins ->
                favoriteCoins.map { coin -> mapper.mapToState(coin) }
            }
            .onEach { favoriteCoinsState ->
                reduce {
                    state.copy(
                        favoriteCoins = favoriteCoinsState,
                        isLoading = false,
                        error = null
                    )
                }
            }
            .catch { throwable ->
                reduce {
                    state.copy(
                        isLoading = false,
                        error = throwable.message ?: "Failed to load favorites"
                    )
                }
                postSideEffect(
                    SideEffect.ShowError(
                        throwable.message ?: "Failed to load favorites"
                    )
                )
            }
            .launchIn(viewModelScope)
    }

    fun removeFavourite(coinId: String) = intent {
        viewModelScope.launch {
            kotlin.runCatching {
                unsetFavouriteCoinUseCase(coinId)
            }.fold(
                onSuccess = {
                    // Удаляем монету из списка
                    val updatedFavorites = state.favoriteCoins.filter { it.id != coinId }
                    reduce { state.copy(favoriteCoins = updatedFavorites) }
                    postSideEffect(SideEffect.RemoveFavouriteSuccess)
                },
                onFailure = { throwable ->
                    postSideEffect(
                        SideEffect.RemoveFavouriteError(
                            throwable.message ?: "Failed to remove from favorites"
                        )
                    )
                }
            )
        }
    }

    fun refresh() = loadFavorites()
}