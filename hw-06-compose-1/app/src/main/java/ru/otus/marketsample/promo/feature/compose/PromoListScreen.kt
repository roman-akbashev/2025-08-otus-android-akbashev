package ru.otus.marketsample.promo.feature.compose

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
import ru.otus.marketsample.promo.feature.PromoScreenState
import ru.otus.marketsample.promo.feature.PromoState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromoListScreen(
    state: PromoScreenState,
    errorHasShown: () -> Unit,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
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
@Preview
private fun PromoListScreenPreview() {
    PromoListScreen(
        state = PromoScreenState(
            isLoading = false,
            promoListState = List(10) {
                PromoState(
                    id = it.toString(),
                    name = "Name $it",
                    description = "description",
                    image = "image"
                )
            },
            hasError = false,
        ),
        errorHasShown = { },
        isRefreshing = false,
        onRefresh = { },
    )
}