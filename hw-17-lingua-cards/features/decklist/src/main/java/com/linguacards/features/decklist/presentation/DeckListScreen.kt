package com.linguacards.features.decklist.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
    onCreateDeck: () -> Unit,
    onDeckLongPress: (Deck) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("LinguaCards") },
                actions = {
                    IconButton(onClick = { /* Поиск */ }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                }
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (state) {
                is DecksState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is DecksState.Success -> {
                    val decks = (state as DecksState.Success).decks

                    if (decks.isEmpty()) {
                        EmptyDeckListContent()
                    } else {
                        DeckListContent(
                            decks = decks,
                            onDeckClick = onDeckClick,
                            onDeckLongPress = onDeckLongPress
                        )
                    }
                }

                is DecksState.Error -> {
                    ErrorContent(
                        message = (state as DecksState.Error).message,
                        onRetry = { viewModel.loadDecks() }
                    )
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateDeckDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name, description ->
                viewModel.createDeck(name, description)
                showCreateDialog = false
            }
        )
    }
}

@Composable
fun EmptyDeckListContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
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
fun DeckListContent(
    decks: List<Deck>,
    onDeckClick: (Long) -> Unit,
    onDeckLongPress: (Deck) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(decks) { deck ->
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
            .clickable { onClick() }
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Error",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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

private fun formatDate(instant: Instant): String {
    val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return "${dateTime.dayOfMonth}.${dateTime.monthNumber}.${dateTime.year}"
}

@Preview(showBackground = true, name = "DeckListScreen - Loading State")
@Composable
fun PreviewDeckListScreenLoading() {
    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
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

@Preview(showBackground = true, name = "DeckListScreen - With Decks")
@Composable
fun PreviewDeckListScreenWithDecks() {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            DeckListContent(
                decks = previewDecks,
                onDeckClick = {},
                onDeckLongPress = {}
            )
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

@Preview(showBackground = true, name = "Deck Item - With Description")
@Composable
fun PreviewDeckItemWithDescription() {
    MaterialTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            DeckItem(
                deck = previewDecks[0],
                onClick = {},
                onLongPress = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "Deck Item - Without Description")
@Composable
fun PreviewDeckItemWithoutDescription() {
    MaterialTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            DeckItem(
                deck = previewDecks[1],
                onClick = {},
                onLongPress = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "Deck Item - Zero Cards")
@Composable
fun PreviewDeckItemZeroCards() {
    MaterialTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            DeckItem(
                deck = previewDecks[2],
                onClick = {},
                onLongPress = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "Create Deck Dialog")
@Composable
fun PreviewCreateDeckDialog() {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxSize()) {
                CreateDeckDialog(
                    onDismiss = {},
                    onCreate = { _, _ -> }
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Empty Deck Content")
@Composable
fun PreviewEmptyDeckContent() {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            EmptyDeckListContent()
        }
    }
}

@Preview(showBackground = true, name = "Error Content")
@Composable
fun PreviewErrorContent() {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            ErrorContent(
                message = "Network error occurred",
                onRetry = {}
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "Scaffold with TopBar and FAB")
@Composable
fun PreviewDeckListScaffold() {
    MaterialTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("LinguaCards") },
                    actions = {
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = {}) {
                    Icon(Icons.Default.Add, contentDescription = "Create deck")
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Text("Content goes here", modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(
    showBackground = true,
    name = "DeckListScreen - Full with Decks",
    device = "spec:width=411dp,height=891dp"
)
@Composable
fun PreviewDeckListScreenFull() {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("LinguaCards") },
                        actions = {
                            IconButton(onClick = {}) {
                                Icon(Icons.Default.Search, contentDescription = "Search")
                            }
                        }
                    )
                },
                floatingActionButton = {
                    FloatingActionButton(onClick = {}) {
                        Icon(Icons.Default.Add, contentDescription = "Create deck")
                    }
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    DeckListContent(
                        decks = previewDecks,
                        onDeckClick = {},
                        onDeckLongPress = {}
                    )
                }
            }
        }
    }
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
    ),
    Deck(
        id = 4,
        name = "Italian Phrases",
        description = "Common travel phrases for Italy",
        createdAt = now,
        updatedAt = now,
        cardCount = 56
    ),
    Deck(
        id = 5,
        name = "Japanese Kanji",
        description = "N5 level kanji characters",
        createdAt = now,
        updatedAt = now,
        cardCount = 80
    )
)