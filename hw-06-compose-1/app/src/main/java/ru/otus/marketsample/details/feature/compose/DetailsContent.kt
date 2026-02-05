package ru.otus.marketsample.details.feature.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import ru.otus.common.Colors
import ru.otus.marketsample.details.feature.DetailsScreenState
import ru.otus.marketsample.details.feature.DetailsState
import ru.otus.marketsample.products.feature.compose.Discount

@Composable
fun Content(
    state: DetailsScreenState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End
    ) {

        AsyncImage(
            model = state.detailsState.image,
            contentDescription = null,
            modifier = Modifier.height(300.dp)
        )

        Row {
            Text(
                text = state.detailsState.name,
                fontSize = 24.sp,
            )
            Spacer(modifier = Modifier.weight(1f))
        }

        if (state.detailsState.discount.isNotBlank()) {
            Discount(
                state.detailsState.discount,
                modifier = Modifier
            )
        }

        Text(
            text = state.detailsState.price,
            color = Colors.PURPLE_500.color,
            fontSize = 18.sp,
            modifier = Modifier.padding(14.dp)
        )

        Button(
            onClick = { },
            shape = RoundedCornerShape(5.dp),
            modifier = Modifier.padding(10.dp)
        ) {
            Text(text = "ADD TO CART", fontSize = 18.sp)
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun ContentPreview() {
    Content(
        state = DetailsScreenState(
            isLoading = false,
            hasError = false,
            detailsState = DetailsState(
                id = "1",
                name = "Товар",
                image = "",
                price = "1000 р",
                hasDiscount = true,
                discount = "-10%"
            ),
            errorProvider = { "" }
        ),
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    )
}