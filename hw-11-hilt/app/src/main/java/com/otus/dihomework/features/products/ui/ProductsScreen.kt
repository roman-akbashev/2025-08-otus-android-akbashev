package com.otus.dihomework.features.products.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.otus.dihomework.features.products.ProductsScreenState
import com.otus.dihomework.features.products.ProductsViewModel
import com.otus.dihomework.features.products.ProductsViewModelFactory

@Composable
fun ProductsScreenContent(
    modifier: Modifier = Modifier
) {
    val viewModel: ProductsViewModel = viewModel(
        factory = ProductsViewModelFactory()
    )

    val state by viewModel.state.collectAsState()

    ProductsScreen(
        state = state,
        onToggleFavorite = viewModel::onToggleFavorite,
        modifier = modifier
    )
}

@Composable
fun ProductsScreen(
    state: ProductsScreenState,
    onToggleFavorite: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        when {
            state.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            state.error != null -> {
                Text(
                    text = state.error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = state.products,
                        key = { it.id }
                    ) { product ->
                        ProductCard(
                            product = product,
                            onToggleFavorite = onToggleFavorite
                        )
                    }
                }
            }
        }
    }
}
