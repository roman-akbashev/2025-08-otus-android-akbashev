package com.linguacards.features.deckdetail.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FiberNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Queue
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.linguacards.core.model.Card
import com.linguacards.core.model.Deck
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeckDetailScreen(
    deckId: Long,
    onCardClick: (Long) -> Unit,
    onAddCard: () -> Unit,
    onStartStudy: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DeckDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var showDeleteDialog by remember { mutableStateOf<Card?>(null) }

    Scaffold(
        modifier = modifier,
        topBar = {
            DeckDetailTopBar(
                searchQuery = searchQuery,
                onSearchQueryChange = { viewModel.onSearchQueryChanged(it) },
                onBackClick = onBackClick,
                onStartStudy = onStartStudy
            )
        }, floatingActionButton = {
            FloatingActionButton(
                onClick = onAddCard,
                modifier = Modifier.testTag("add_card_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add card")
            }
        }) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (state) {
                is DeckDetailState.Loading -> {
                    LoadingContent()
                }

                is DeckDetailState.Success -> {
                    val successState = state as DeckDetailState.Success
                    CardsListContent(
                        deck = successState.deck,
                        cards = successState.cards,
                        searchQuery = successState.searchQuery,
                        onCardClick = onCardClick,
                        onCardLongPress = { card -> showDeleteDialog = card },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                is DeckDetailState.Empty -> {
                    EmptyDeckContent(
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }

    if (errorMessage != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearErrorMessage() },
            title = { Text("Error") },
            text = { Text(errorMessage ?: "Unknown error") },
            confirmButton = {
                TextButton(onClick = { viewModel.clearErrorMessage() }) {
                    Text("OK")
                }
            }
        )
    }

    showDeleteDialog?.let { card ->
        DeleteCardDialog(card = card, onConfirm = {
            viewModel.deleteCard(card)
            showDeleteDialog = null
        }, onDismiss = { showDeleteDialog = null })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeckDetailTopBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onBackClick: () -> Unit,
    onStartStudy: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isSearching by remember { mutableStateOf(false) }

    TopAppBar(
        modifier = modifier,
        title = {
            if (isSearching) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Search cards...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        cursorColor = MaterialTheme.colorScheme.primary
                    ),
                    textStyle = MaterialTheme.typography.bodyMedium,
                    shape = MaterialTheme.shapes.small
                )
            } else {
                Text(
                    text = "Deck Details",
                    style = MaterialTheme.typography.titleLarge
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            if (isSearching) {
                IconButton(onClick = {
                    isSearching = false
                    onSearchQueryChange("")
                }) {
                    Icon(Icons.Default.Close, contentDescription = "Close search")
                }
            } else {
                IconButton(onClick = { isSearching = true }) {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                }
            }
            IconButton(onClick = onStartStudy) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Study")
            }
        }
    )
}

@Composable
fun CardsListContent(
    deck: Deck,
    cards: List<Card>,
    searchQuery: String,
    onCardClick: (Long) -> Unit,
    onCardLongPress: (Card) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        CardStats(
            totalCards = deck.cardCount,
            filteredCount = cards.size,
            isFiltered = searchQuery.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .wrapContentHeight()
        )

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (cards.isEmpty()) {
                item {
                    if (searchQuery.isNotBlank()) {
                        NoSearchResults(
                            query = searchQuery,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        EmptyDeckContent()
                    }
                }
            } else {
                items(
                    items = cards, key = { it.id }) { card ->
                    CardItem(
                        card = card,
                        onClick = { onCardClick(card.id) },
                        onLongPress = { onCardLongPress(card) })
                }
            }
        }
    }
}

@Composable
fun CardStats(
    totalCards: Int,
    filteredCount: Int,
    isFiltered: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatItem(
                value = totalCards.toString(), label = "Total Cards"
            )

            if (isFiltered) {
                VerticalDivider()
                StatItem(
                    value = filteredCount.toString(), label = "Filtered"
                )
            }
        }
    }
}

@Composable
fun StatItem(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun CardItem(
    card: Card,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick, onLongClick = onLongPress
            ), elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Слово и транскрипция
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Text(
                        text = card.word,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    card.transcription?.let {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "[$it]",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Перевод
                Text(
                    text = card.translation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                // Пример (если есть)
                card.example?.let {
                    Text(
                        text = "example: $it",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Информация о повторении
                ReviewInfo(card = card)
            }

            // Индикатор прогресса
            ReviewProgressBadge(card = card)
        }
    }
}

@Composable
fun ReviewInfo(
    card: Card,
    modifier: Modifier = Modifier
) {
    val reviewDateText = if (card.nextReviewDate != null) {
        val now = kotlinx.datetime.Clock.System.now()
        val nextDate = card.nextReviewDate!!

        val daysUntil = calculateDaysUntil(now, nextDate)
        val timeString = formatTime(nextDate)

        when {
            daysUntil < 0 -> {
                when (val overdueDays = -daysUntil) {
                    1 -> "Просрочено на 1 день"
                    in 2..4 -> "Просрочено на $overdueDays дня"
                    else -> "Просрочено на $overdueDays дней"
                }
            }

            daysUntil == 0 -> {
                if (timeString != null) "Сегодня в $timeString" else "Сегодня"
            }

            daysUntil == 1 -> {
                if (timeString != null) "Завтра в $timeString" else "Завтра"
            }

            else -> "Через $daysUntil дн."
        }
    } else {
        "Новая карточка"
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (card.repetitions == 0 && card.nextReviewDate == null) Icons.Default.FiberNew else Icons.Default.Update,
            contentDescription = null,
            modifier = Modifier.size(12.dp),
            tint = when {
                card.repetitions == 0 && card.nextReviewDate == null -> MaterialTheme.colorScheme.tertiary
                card.nextReviewDate != null && card.nextReviewDate!! <= kotlinx.datetime.Clock.System.now() -> MaterialTheme.colorScheme.error

                else -> MaterialTheme.colorScheme.primary
            }
        )

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = reviewDateText, style = MaterialTheme.typography.labelSmall, color = when {
                card.repetitions == 0 && card.nextReviewDate == null -> MaterialTheme.colorScheme.tertiary
                card.nextReviewDate != null && card.nextReviewDate!! <= kotlinx.datetime.Clock.System.now() -> MaterialTheme.colorScheme.error

                else -> MaterialTheme.colorScheme.primary
            }
        )
    }
}

private fun calculateDaysUntil(now: Instant, nextDate: Instant): Int {
    val nowMillis = now.toEpochMilliseconds()
    val nextMillis = nextDate.toEpochMilliseconds()

    // Округляем вверх для положительных значений, вниз для отрицательных
    return when {
        nextMillis <= nowMillis -> {
            // Уже просрочено - отрицательное количество дней
            ((nextMillis - nowMillis) / (1000 * 60 * 60 * 24)).toInt()
        }

        else -> {
            // Будущая дата - округляем вверх до целого дня
            val diffMillis = nextMillis - nowMillis
            ((diffMillis + (1000 * 60 * 60 * 24 - 1)) / (1000 * 60 * 60 * 24)).toInt()
        }
    }
}

private fun formatTime(instant: Instant): String? {
    return try {
        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        val hour = localDateTime.hour
        val minute = localDateTime.minute

        when {
            minute == 0 -> "$hour:00"
            minute < 10 -> "$hour:0$minute"
            else -> "$hour:$minute"
        }
    } catch (_: Exception) {
        null
    }
}

@Composable
fun ReviewProgressBadge(
    card: Card,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(32.dp)
            .background(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), shape = CircleShape
            ), contentAlignment = Alignment.Center
    ) {
        Text(
            text = card.repetitions.toString(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun EmptyDeckContent(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Queue,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No cards yet", style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Add your first card to start learning",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun NoSearchResults(
    query: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(32.dp)
            .wrapContentHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.SearchOff,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No results for \"$query\"",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Try a different search term",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun LoadingContent(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(16.dp))
        Text("Loading deck...")
    }
}

@Composable
fun DeleteCardDialog(
    card: Card,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = { Text("Delete Card") },
        text = {
            Text("Are you sure you want to delete \"${card.word}\"?")
        }, confirmButton = {
            TextButton(
                onClick = onConfirm, colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Delete")
            }
        }, dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        })
}

@Composable
fun VerticalDivider(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(1.dp)
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
    )
}