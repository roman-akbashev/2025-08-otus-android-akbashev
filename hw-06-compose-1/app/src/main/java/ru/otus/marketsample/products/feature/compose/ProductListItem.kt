package ru.otus.marketsample.products.feature.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import ru.otus.common.Colors
import ru.otus.marketsample.products.feature.ProductState

@Composable
fun ProductListItem(
    productState: ProductState,
    onItemClick: (id: String) -> Unit,
    modifier: Modifier = Modifier,
) {

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp, horizontal = 16.dp)
            .height(130.dp)
            .clickable { onItemClick.invoke(productState.id) }
    ) {
        Box(modifier = Modifier.weight(1f)) {
            AsyncImage(
                model = productState.image,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(shape = RoundedCornerShape(12.dp))
            )

            if (productState.discount.isNotBlank()) {
                Discount(productState.discount, Modifier.align(Alignment.TopEnd))
            }
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = productState.name,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontSize = 18.sp,
            )
            Row {
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = productState.price,
                    color = Colors.PURPLE_500.color,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .background(
                            color = Colors.PRICE_BACKGROUND.color,
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(vertical = 8.dp, horizontal = 12.dp)

                )
            }
        }
    }
}

@Composable
fun Discount(
    discount: String,
    modifier: Modifier = Modifier,
) {
    val roundedCornerShape = RoundedCornerShape(
        topEnd = 6.dp,
        topStart = 25.dp,
        bottomStart = 25.dp,
        bottomEnd = 20.dp
    )

    Text(
        text = discount,
        color = Color.White,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        modifier = modifier
            .padding(vertical = 8.dp, horizontal = 8.dp)
            .clip(roundedCornerShape)
            .drawBehind {
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(Colors.PURPLE_200.color, Colors.PURPLE_500.color),
                        start = Offset(0f, size.height),
                        end = Offset(size.width, 0f)
                    ),
                )
            }
            .border(width = 2.dp, color = Color.White, shape = roundedCornerShape)
            .padding(vertical = 4.dp, horizontal = 10.dp)
    )
}

@Composable
@Preview
private fun ProductListItemPreview() {
    ProductListItem(
        productState = ProductState(
            id = "0",
            name = "Product Name",
            image = "",
            price = "2000 руб",
            hasDiscount = true,
            discount = "-20%"
        ),
        onItemClick = {}
    )
}