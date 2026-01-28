package ru.otus.marketsample.products.feature.compose

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import ru.otus.marketsample.products.feature.ProductState
import ru.otus.marketsample.products.feature.ProductsScreenState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(
    state: ProductsScreenState,
    errorHasShown: () -> Unit,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onItemClick: (id: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    Box(modifier.fillMaxSize()) {
        when {
            state.hasError -> {
                Toast.makeText(context, state.errorProvider(context), Toast.LENGTH_SHORT).show()
                errorHasShown()
            }

            state.isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            else -> {
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
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

@Preview
@Composable
private fun ProductListScreenPreview() {
    ProductListScreen(
        state = ProductsScreenState(
            isLoading = false,
            productListState = List(10) {
                ProductState(
                    id = it.toString(),
                    name = "Product Name $it",
                    image = "",
                    price = "2000 руб",
                    hasDiscount = true,
                    discount = "-20%"
                )
            },
            hasError = false,
            errorProvider = { "" }
        ),
        errorHasShown = {},
        isRefreshing = false,
        onRefresh = { },
        onItemClick = { },
    )
}