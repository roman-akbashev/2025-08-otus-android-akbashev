package ru.otus.marketsample.details.feature.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.otus.common.ErrorHandler
import ru.otus.marketsample.details.feature.DetailsScreenState
import ru.otus.marketsample.details.feature.DetailsState

@Composable
fun DetailsScreen(
    state: DetailsScreenState,
    errorHasShown: () -> Unit,
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
                Content(
                    state = state,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                )
            }
        }
    }
}

@Composable
@Preview(name = "Loading State", showBackground = true)
private fun DetailsScreenLoadingPreview() {
    DetailsScreen(
        state = DetailsScreenState(
            isLoading = true,
            hasError = false,
            detailsState = DetailsState(),
            errorProvider = { "Error message" }
        ),
        errorHasShown = { }
    )
}

@Composable
@Preview(name = "Error State", showBackground = true)
private fun DetailsScreenErrorPreview() {
    DetailsScreen(
        state = DetailsScreenState(
            isLoading = false,
            hasError = true,
            detailsState = DetailsState(),
            errorProvider = { "Ошибка загрузки данных. Проверьте соединение с интернетом." }
        ),
        errorHasShown = { }
    )
}

@Composable
@Preview(name = "Success State with Discount", showBackground = true)
private fun DetailsScreenSuccessWithDiscountPreview() {
    DetailsScreen(
        state = DetailsScreenState(
            isLoading = false,
            hasError = false,
            detailsState = DetailsState(
                id = "1",
                name = "Смартфон Samsung Galaxy S23 Ultra 12/256GB Green",
                image = "https://example.com/phone.jpg",
                price = "89 990 ₽",
                hasDiscount = true,
                discount = "-15%"
            ),
            errorProvider = { "Error message" }
        ),
        errorHasShown = { }
    )
}

@Composable
@Preview(name = "Success State without Discount", showBackground = true)
private fun DetailsScreenSuccessWithoutDiscountPreview() {
    DetailsScreen(
        state = DetailsScreenState(
            isLoading = false,
            hasError = false,
            detailsState = DetailsState(
                id = "2",
                name = "Наушники Apple AirPods Pro 2",
                image = "https://example.com/airpods.jpg",
                price = "24 990 ₽",
                hasDiscount = false,
                discount = ""
            ),
            errorProvider = { "Error message" }
        ),
        errorHasShown = { }
    )
}

@Composable
@Preview(name = "Empty Success State", showBackground = true)
private fun DetailsScreenEmptyPreview() {
    DetailsScreen(
        state = DetailsScreenState(
            isLoading = false,
            hasError = false,
            detailsState = DetailsState(
                id = "3",
                name = "",
                image = "",
                price = "",
                hasDiscount = false,
                discount = ""
            ),
            errorProvider = { "Error message" }
        ),
        errorHasShown = { }
    )
}