package ru.otus.marketsample.products.feature.compose

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
import ru.otus.marketsample.products.feature.ProductState
import ru.otus.marketsample.products.feature.ProductsScreenState

@Composable
fun ProductListScreen(
    state: ProductsScreenState,
    errorHasShown: () -> Unit,
    onRefresh: () -> Unit,
    onItemClick: (id: String) -> Unit,
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
                    isRefreshing = state.isRefreshing,
                    onRefresh = onRefresh,
                ) {
                    LazyColumn {
                        items(state.productListState, { it.id }) { productState ->
                            ProductListItem(
                                productState = productState,
                                onItemClick = onItemClick,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(name = "Loading State", showBackground = true)
@Composable
private fun ProductListScreen_LoadingPreview() {
    ProductListScreen(
        state = ProductsScreenState(
            isLoading = true,
            productListState = emptyList(),
            hasError = false,
            errorProvider = { "" }
        ),
        errorHasShown = {},
        onRefresh = { },
        onItemClick = { },
    )
}

@Preview(name = "Normal State", showBackground = true)
@Composable
private fun ProductListScreen_NormalPreview() {
    ProductListScreen(
        state = ProductsScreenState(
            isLoading = false,
            productListState = List(10) {
                ProductState(
                    id = it.toString(),
                    name = "Product Name $it",
                    image = "",
                    price = "2000 руб",
                    hasDiscount = it % 3 == 0,
                    discount = if (it % 3 == 0) "-${(it + 10)}%" else ""
                )
            },
            hasError = false,
            errorProvider = { "" }
        ),
        errorHasShown = {},
        onRefresh = { },
        onItemClick = { },
    )
}

@Preview(name = "Empty State", showBackground = true)
@Composable
private fun ProductListScreen_EmptyPreview() {
    ProductListScreen(
        state = ProductsScreenState(
            isLoading = false,
            productListState = emptyList(),
            hasError = false,
            errorProvider = { "" }
        ),
        errorHasShown = {},
        onRefresh = { },
        onItemClick = { },
    )
}

@Preview(name = "Error State", showBackground = true)
@Composable
private fun ProductListScreen_ErrorPreview() {
    ProductListScreen(
        state = ProductsScreenState(
            isLoading = false,
            productListState = emptyList(),
            hasError = true,
            errorProvider = { "Произошла ошибка при загрузке данных" }
        ),
        errorHasShown = {},
        onRefresh = { },
        onItemClick = { },
    )
}

@Preview(name = "Refreshing State", showBackground = true)
@Composable
private fun ProductListScreen_RefreshingPreview() {
    ProductListScreen(
        state = ProductsScreenState(
            isLoading = false,
            productListState = List(5) {
                ProductState(
                    id = it.toString(),
                    name = "Product Name $it",
                    image = "",
                    price = "${(it + 1) * 1000} руб",
                    hasDiscount = it % 2 == 0,
                    discount = if (it % 2 == 0) "-15%" else ""
                )
            },
            hasError = false,
            errorProvider = { "" }
        ),
        errorHasShown = {},
        onRefresh = { },
        onItemClick = { },
    )
}

@Preview(name = "Single Item", showBackground = true)
@Composable
private fun ProductListScreen_SingleItemPreview() {
    ProductListScreen(
        state = ProductsScreenState(
            isLoading = false,
            productListState = listOf(
                ProductState(
                    id = "1",
                    name = "Product with long name that should be truncated",
                    image = "",
                    price = "9999 руб",
                    hasDiscount = true,
                    discount = "-50%"
                )
            ),
            hasError = false,
            errorProvider = { "" }
        ),
        errorHasShown = {},
        onRefresh = { },
        onItemClick = { },
    )
}

@Preview(name = "Many Items", showBackground = true)
@Composable
private fun ProductListScreen_ManyItemsPreview() {
    ProductListScreen(
        state = ProductsScreenState(
            isLoading = false,
            productListState = List(50) {
                ProductState(
                    id = it.toString(),
                    name = "Product #${it + 1}",
                    image = "",
                    price = "${(it % 10 + 1) * 500} руб",
                    hasDiscount = it % 4 == 0,
                    discount = if (it % 4 == 0) "-${(it % 20 + 5)}%" else ""
                )
            },
            hasError = false,
            errorProvider = { "" }
        ),
        errorHasShown = {},
        onRefresh = { },
        onItemClick = { },
    )
}

@Preview(name = "Loading with Error", showBackground = true)
@Composable
private fun ProductListScreen_LoadingWithErrorPreview() {
    ProductListScreen(
        state = ProductsScreenState(
            isLoading = true,
            productListState = emptyList(),
            hasError = true,
            errorProvider = { "Network error occurred" }
        ),
        errorHasShown = {},
        onRefresh = { },
        onItemClick = { },
    )
}