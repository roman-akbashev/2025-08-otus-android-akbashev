package ru.otus.marketsample.promo.feature.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import ru.otus.common.ErrorHandler
import ru.otus.marketsample.promo.feature.PromoScreenState
import ru.otus.marketsample.promo.feature.PromoState

@Composable
fun PromoListScreen(
    state: PromoScreenState,
    errorHasShown: () -> Unit,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ErrorHandler(
        hasError = state.hasError,
        errorMessage = state.errorProvider(LocalContext.current),
        onErrorShown = errorHasShown,
        modifier = modifier.fillMaxSize()
    ) {
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            else -> {
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = onRefresh,
                ) {
                    LazyColumn {
                        items(state.promoListState, { it.id }) { promoState ->
                            PromoListItem(promoState)
                        }
                    }
                }
            }
        }
    }
}

@Composable
@Preview(name = "Loading State", showBackground = true)
private fun PromoListScreenLoadingPreview() {
    PromoListScreen(
        state = PromoScreenState(
            isLoading = true,
            promoListState = emptyList(),
            hasError = false,
            errorProvider = { "Error message" }
        ),
        errorHasShown = { },
        isRefreshing = false,
        onRefresh = { },
    )
}

@Composable
@Preview(name = "With Data", showBackground = true)
private fun PromoListScreenWithDataPreview() {
    PromoListScreen(
        state = PromoScreenState(
            isLoading = false,
            promoListState = List(10) {
                PromoState(
                    id = it.toString(),
                    name = "Promo Name $it",
                    description = "Detailed description of promo item $it that might be longer to show how text wraps",
                    image = "https://example.com/image$it.jpg"
                )
            },
            hasError = false,
            errorProvider = { "Error message" }
        ),
        errorHasShown = { },
        isRefreshing = false,
        onRefresh = { },
    )
}

@Composable
@Preview(name = "With Few Items", showBackground = true)
private fun PromoListScreenWithFewItemsPreview() {
    PromoListScreen(
        state = PromoScreenState(
            isLoading = false,
            promoListState = List(3) {
                PromoState(
                    id = it.toString(),
                    name = "Short Promo $it",
                    description = "Short description",
                    image = "image_url"
                )
            },
            hasError = false,
            errorProvider = { "Error message" }
        ),
        errorHasShown = { },
        isRefreshing = false,
        onRefresh = { },
    )
}

@Composable
@Preview(name = "With Single Item", showBackground = true)
private fun PromoListScreenWithSingleItemPreview() {
    PromoListScreen(
        state = PromoScreenState(
            isLoading = false,
            promoListState = listOf(
                PromoState(
                    id = "1",
                    name = "Single Promo Item",
                    description = "This is a detailed description of the only promo item available in the list",
                    image = "single_image.jpg"
                )
            ),
            hasError = false,
            errorProvider = { "Error message" }
        ),
        errorHasShown = { },
        isRefreshing = false,
        onRefresh = { },
    )
}

@Composable
@Preview(name = "Empty State", showBackground = true)
private fun PromoListScreenEmptyPreview() {
    PromoListScreen(
        state = PromoScreenState(
            isLoading = false,
            promoListState = emptyList(),
            hasError = false,
            errorProvider = { "Error message" }
        ),
        errorHasShown = { },
        isRefreshing = false,
        onRefresh = { },
    )
}

@Composable
@Preview(name = "Error State", showBackground = true)
private fun PromoListScreenErrorPreview() {
    PromoListScreen(
        state = PromoScreenState(
            isLoading = false,
            promoListState = emptyList(),
            hasError = true,
            errorProvider = { "Failed to load promo items. Please check your internet connection and try again." }
        ),
        errorHasShown = { },
        isRefreshing = false,
        onRefresh = { },
    )
}

@Composable
@Preview(name = "Refreshing State", showBackground = true)
private fun PromoListScreenRefreshingPreview() {
    PromoListScreen(
        state = PromoScreenState(
            isLoading = false,
            promoListState = List(5) {
                PromoState(
                    id = it.toString(),
                    name = "Promo during refresh $it",
                    description = "Description while refreshing",
                    image = "image_$it"
                )
            },
            hasError = false,
            errorProvider = { "Error message" }
        ),
        errorHasShown = { },
        isRefreshing = true,
        onRefresh = { },
    )
}

@Composable
@Preview(name = "Error with Existing Data", showBackground = true)
private fun PromoListScreenErrorWithDataPreview() {
    PromoListScreen(
        state = PromoScreenState(
            isLoading = false,
            promoListState = List(4) {
                PromoState(
                    id = it.toString(),
                    name = "Existing Promo $it",
                    description = "These items were loaded before error occurred",
                    image = "existing_$it"
                )
            },
            hasError = true,
            errorProvider = { "Failed to load new items, but showing cached data" }
        ),
        errorHasShown = { },
        isRefreshing = false,
        onRefresh = { },
    )
}

@Composable
@Preview(name = "Long Text Items", showBackground = true)
private fun PromoListScreenLongTextPreview() {
    PromoListScreen(
        state = PromoScreenState(
            isLoading = false,
            promoListState = List(4) {
                PromoState(
                    id = it.toString(),
                    name = "Very Long Promo Item Name That Might Extend Beyond One Line $it",
                    description = "This is an extremely detailed and lengthy description that aims to test how the UI handles multiline text content. It includes multiple sentences and should demonstrate text wrapping behavior in the promo list item component. Let's add even more text to make sure we cover edge cases.",
                    image = "long_image_url_$it"
                )
            },
            hasError = false,
            errorProvider = { "Error message" }
        ),
        errorHasShown = { },
        isRefreshing = false,
        onRefresh = { },
    )
}