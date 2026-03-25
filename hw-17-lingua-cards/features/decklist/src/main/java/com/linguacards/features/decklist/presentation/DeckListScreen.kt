package com.linguacards.features.decklist.presentation

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
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
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.linguacards.core.model.Deck
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeckListScreen(
    viewModel: DecksViewModel = hiltViewModel(),
    onDeckClick: (Long) -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val allDecksCount by viewModel.allDecksCount.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf<Deck?>(null) }

    Scaffold(
        topBar = {
            DeckListTopBar(
                searchQuery = searchQuery,
                onSearchQueryChange = { viewModel.onSearchQueryChanged(it) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                modifier = Modifier.testTag("create_deck_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create deck")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (state) {
                is DecksState.Loading -> {
                    LoadingContent(modifier = Modifier.fillMaxSize())
                }

                is DecksState.Success -> {
                    val successState = state as DecksState.Success

                    // Статистика колод
                    DeckStats(
                        totalDecks = allDecksCount,
                        filteredCount = successState.decks.size,
                        isFiltered = successState.searchQuery.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .wrapContentHeight()
                    )

                    if (successState.decks.isEmpty() && successState.searchQuery.isNotBlank()) {
                        NoSearchResults(
                            query = successState.searchQuery,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        DeckListContent(
                            decks = successState.decks,
                            onDeckClick = onDeckClick,
                            onDeckLongPress = { deck -> showDeleteDialog = deck },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                is DecksState.Empty -> {
                    EmptyDeckListContent(modifier = Modifier.fillMaxSize())
                }

                is DecksState.Error -> {
                    ErrorContent(
                        message = (state as DecksState.Error).message,
                        onRetry = { viewModel.retry() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }

    // Диалог создания колоды
    if (showCreateDialog) {
        CreateDeckDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name, description ->
                viewModel.createDeck(name, description)
                showCreateDialog = false
            }
        )
    }

    // Диалог подтверждения удаления
    showDeleteDialog?.let { deck ->
        DeleteDeckDialog(
            deck = deck,
            onConfirm = {
                viewModel.deleteDeck(deck)
                showDeleteDialog = null
            },
            onDismiss = { showDeleteDialog = null }
        )
    }
}

@Composable
fun DeckStats(
    totalDecks: Int,
    filteredCount: Int,
    isFiltered: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .wrapContentHeight(),
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
                value = totalDecks.toString(),
                label = "Total Decks"
            )

            if (isFiltered) {
                VerticalDivider()
                StatItem(
                    value = filteredCount.toString(),
                    label = "Filtered"
                )
            }
        }
    }
}

@Composable
fun StatItem(
    value: String,
    label: String
) {
    Column(
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
fun VerticalDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeckListTopBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit
) {
    var isSearching by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            if (isSearching) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Search decks...") },
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
                    text = "LinguaCards",
                    style = MaterialTheme.typography.titleLarge
                )
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
        }
    )
}

@Composable
fun DeckListContent(
    decks: List<Deck>,
    onDeckClick: (Long) -> Unit,
    onDeckLongPress: (Deck) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = decks,
            key = { it.id }
        ) { deck ->
            DeckItem(
                deck = deck,
                onClick = { onDeckClick(deck.id) },
                onLongPress = { onDeckLongPress(deck) }
            )
        }
    }
}

@Composable
fun DeckItem(
    deck: Deck,
    onClick: () -> Unit,
    onLongPress: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongPress
            )
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
                Text(
                    text = deck.name,
                    style = MaterialTheme.typography.titleMedium
                )

                deck.description?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${deck.cardCount} cards • Updated ${formatDate(deck.updatedAt)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Badge(
                modifier = Modifier.padding(start = 8.dp),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Text("${deck.cardCount}")
            }
        }
    }
}

@Composable
fun EmptyDeckListContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
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
            text = "No decks yet",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Create your first deck to start learning",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun NoSearchResults(
    query: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
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
fun LoadingContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(16.dp))
        Text("Loading decks...")
    }
}

@Composable
fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Error",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

@Composable
fun CreateDeckDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Deck") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Deck name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onCreate(name, description.takeIf { it.isNotBlank() }) },
                enabled = name.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun DeleteDeckDialog(
    deck: Deck,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Deck") },
        text = {
            Text("Are you sure you want to delete \"${deck.name}\"? This will also delete all cards in this deck.")
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun formatDate(instant: Instant): String {
    val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return "${dateTime.dayOfMonth}.${dateTime.monthNumber}.${dateTime.year}"
}

// Preview data
private val now = Clock.System.now()

private val previewDecks = listOf(
    Deck(
        id = 1,
        name = "Spanish Basics",
        description = "Essential Spanish vocabulary for beginners. Learn greetings, numbers, and common phrases.",
        createdAt = now,
        updatedAt = now,
        cardCount = 42
    ),
    Deck(
        id = 2,
        name = "French Vocabulary",
        description = null,
        createdAt = now,
        updatedAt = now,
        cardCount = 128
    ),
    Deck(
        id = 3,
        name = "German Articles",
        description = "Practice der, die, das with common nouns",
        createdAt = now,
        updatedAt = now,
        cardCount = 0
    )
)

@Preview(showBackground = true, name = "DeckListScreen - Loading State")
@Composable
fun PreviewDeckListScreenLoading() {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            LoadingContent()
        }
    }
}

@Preview(showBackground = true, name = "DeckListScreen - Empty State")
@Composable
fun PreviewDeckListScreenEmpty() {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            EmptyDeckListContent()
        }
    }
}

@Preview(showBackground = true, name = "DeckListScreen - No Search Results")
@Composable
fun PreviewDeckListScreenNoResults() {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            NoSearchResults(query = "nonexistent")
        }
    }
}

@Preview(showBackground = true, name = "DeckListScreen - With Decks")
@Composable
fun PreviewDeckListScreenWithDecks() {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column {
                DeckStats(
                    totalDecks = 5,
                    filteredCount = 3,
                    isFiltered = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
                DeckListContent(
                    decks = previewDecks,
                    onDeckClick = {},
                    onDeckLongPress = {}
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "DeckListScreen - Error State")
@Composable
fun PreviewDeckListScreenError() {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            ErrorContent(
                message = "Failed to load decks. Check your connection.",
                onRetry = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "Deck Stats")
@Composable
fun PreviewDeckStats() {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DeckStats(
                    totalDecks = 10,
                    filteredCount = 10,
                    isFiltered = false
                )
                DeckStats(
                    totalDecks = 10,
                    filteredCount = 3,
                    isFiltered = true
                )
            }
        }
    }
}