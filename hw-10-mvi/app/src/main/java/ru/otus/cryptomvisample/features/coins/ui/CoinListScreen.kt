package ru.otus.cryptomvisample.features.coins.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.otus.cryptomvisample.features.coins.CoinListViewModel
import ru.otus.cryptomvisample.ui.theme.TextPrimary

@Composable
fun CoinListScreen(viewModel: CoinListViewModel) {
    val state = viewModel.container.stateFlow.collectAsState().value
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.container.sideEffectFlow.collect { sideEffect ->
            when (sideEffect) {
                is CoinListViewModel.SideEffect.ShowError -> {
                    snackbarHostState.showSnackbar(
                        message = sideEffect.message,
                        duration = SnackbarDuration.Long
                    )
                }

                is CoinListViewModel.SideEffect.ToggleFavouriteError -> {
                    snackbarHostState.showSnackbar(
                        message = sideEffect.message,
                        duration = SnackbarDuration.Short
                    )
                }

                CoinListViewModel.SideEffect.ToggleFavouriteSuccess -> {
                    snackbarHostState.showSnackbar(
                        message = "Favorites updated",
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        if (state.isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            CoinListContent(
                state = state,
                onHighlightMoversToggled = viewModel::toggleHighlightMovers,
                onToggleFavourite = viewModel::toggleFavourite,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
private fun CoinListContent(
    state: CoinListViewModel.State,
    onHighlightMoversToggled: (Boolean) -> Unit,
    onToggleFavourite: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp)
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Coins",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )

            FilterChip(
                onClick = { onHighlightMoversToggled(!state.highlightMovers) },
                label = {
                    Text(
                        text = "Highlight Movers",
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                leadingIcon = {
                    if (state.highlightMovers) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Highlight Movers",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                selected = state.highlightMovers,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            state.categories.forEach { category ->
                item(span = { GridItemSpan(2) }) {
                    CategoryHeader(
                        title = category.name,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
                if (category.coins.size >= 10) {
                    item(span = { GridItemSpan(2) }) {
                        HorizontalCoinList(
                            coins = category.coins,
                            onToggleFavourite = onToggleFavourite
                        )
                    }
                } else {
                    itemsIndexed(
                        items = category.coins,
                        span = { _, _ -> GridItemSpan(1) },
                        key = { _, coin -> coin.id }
                    ) { index, coin ->
                        val (start, end) = if (index % 2 == 0) {
                            16.dp to 0.dp
                        } else {
                            0.dp to 16.dp
                        }
                        CoinCard(
                            coin = coin,
                            onToggleFavourite = { onToggleFavourite(coin.id) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = start, end = end)
                        )
                    }
                }
            }
        }
    }
}