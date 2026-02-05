package com.otus.dihomework.features.favorites.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.otus.dihomework.R
import com.otus.dihomework.features.favorites.FavoritesScreenState
import com.otus.dihomework.features.favorites.FavoritesViewModel
import com.otus.dihomework.features.favorites.FavoritesViewModelFactory

@Composable
fun FavoritesScreenContent(
    modifier: Modifier = Modifier
) {
    val viewModel: FavoritesViewModel = viewModel(
        factory = FavoritesViewModelFactory()
    )

    val state by viewModel.state.collectAsState()

    FavoritesScreen(
        state = state,
        onRemoveFavorite = viewModel::onRemoveFavorite,
        modifier = modifier
    )
}

@Composable
fun FavoritesScreen(
    state: FavoritesScreenState,
    onRemoveFavorite: (String) -> Unit,
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

            state.favorites.isEmpty() -> {
                Text(
                    text = stringResource(R.string.empty_favorites),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = state.favorites,
                        key = { it.id }
                    ) { favorite ->
                        FavoriteProductItem(
                            favorite = favorite,
                            onRemoveFavorite = onRemoveFavorite
                        )
                    }
                }
            }
        }
    }
}
