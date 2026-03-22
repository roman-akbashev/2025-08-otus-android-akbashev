package com.linguacards.features.study.presentation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.linguacards.core.model.Card
import com.linguacards.core.model.SrsGrade
import kotlinx.datetime.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyScreen(
    viewModel: StudyViewModel = hiltViewModel(),
    onFinish: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Study") },
                navigationIcon = {
                    IconButton(onClick = onFinish) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (state) {
                is StudyState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is StudyState.Finished -> {
                    StudyFinishedContent(
                        onFinish = onFinish,
                        onRestart = { viewModel.resetStudy() }
                    )
                }

                is StudyState.Card -> {
                    val cardState = state as StudyState.Card
                    StudyCardContent(
                        card = cardState.card,
                        isFlipped = cardState.isFlipped,
                        progress = cardState.progress,
                        onCardFlip = { viewModel.onCardFlip() },
                        onGradeSelected = { grade -> viewModel.onGradeSelected(grade) }
                    )
                }
            }
        }
    }
}

@Composable
fun StudyCardContent(
    card: Card,
    isFlipped: Boolean,
    progress: String,
    onCardFlip: () -> Unit,
    onGradeSelected: (SrsGrade) -> Unit
) {
    val rotation = animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(300),
        label = "card_rotation"
    )

    // Блокируем касания во время анимации
    val isAnimating = rotation.value != 0f && rotation.value != 180f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Прогресс
        Text(
            text = progress,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Карточка
        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            if (!isAnimating) { // Проверяем, не идет ли анимация
                                onCardFlip()
                            }
                        }
                    )
                }
                .graphicsLayer {
                    rotationY = rotation.value
                    cameraDistance = 12f * density
                },
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            val isFront by remember(rotation.value) {
                derivedStateOf { rotation.value <= 90f }
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .semantics {
                        contentDescription = if (isFront) {
                            "Card front: ${card.word}"
                        } else {
                            "Card back: ${card.translation}"
                        }
                        testTag = "StudyCard" // Для UI тестов
                    },
                contentAlignment = Alignment.Center
            ) {
                if (isFront) {
                    // Лицевая сторона
                    FrontCardContent(card)
                } else {
                    // Оборотная сторона (с учетом переворота)
                    BackCardContent(card)
                }
            }
        }

        // Кнопки оценки (показываем только когда карточка перевернута)
        // Контейнер для кнопок с фиксированной высотой
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp) // Фиксированная высота для кнопок
                .padding(top = 16.dp)
        ) {
            if (isFlipped) {
                GradeButtons(
                    onGradeSelected = onGradeSelected,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Невидимый плейсхолдер того же размера
                Spacer(modifier = Modifier.fillMaxSize())
            }
        }

    }
}

@Composable
fun FrontCardContent(card: Card) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = card.word,
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center
        )

        card.transcription?.let {
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = it,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun BackCardContent(card: Card) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .graphicsLayer {
                rotationY = 180f // Компенсируем поворот карточки
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = card.translation,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        card.example?.let {
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Example:",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = it,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun GradeButtons(
    onGradeSelected: (SrsGrade) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .semantics { testTag = "GradeButtonsRow" }
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        GradeButton(
            text = "Again",
            color = MaterialTheme.colorScheme.error,
            onClick = { onGradeSelected(SrsGrade.AGAIN) },
            modifier = Modifier
                .weight(1f)
                .semantics {
                    testTag = "GradeButton_Again"
                }
        )

        GradeButton(
            text = "Hard",
            color = MaterialTheme.colorScheme.tertiary,
            onClick = { onGradeSelected(SrsGrade.HARD) },
            modifier = Modifier
                .weight(1f)
                .semantics {
                    testTag = "GradeButton_Hard"
                }
        )

        GradeButton(
            text = "Good",
            color = MaterialTheme.colorScheme.primary,
            onClick = { onGradeSelected(SrsGrade.GOOD) },
            modifier = Modifier
                .weight(1f)
                .semantics {
                    testTag = "GradeButton_Good"
                }
        )

        GradeButton(
            text = "Easy",
            color = MaterialTheme.colorScheme.secondary,
            onClick = { onGradeSelected(SrsGrade.EASY) },
            modifier = Modifier
                .weight(1f)
                .semantics {
                    testTag = "GradeButton_Easy"
                }
        )
    }
}

@Composable
fun GradeButton(
    text: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = color
        ),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            fontSize = 12.sp
        )
    }
}

@Composable
fun StudyFinishedContent(
    onFinish: () -> Unit,
    onRestart: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🎉",
            fontSize = 64.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Great job!",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "You've completed all cards for today",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(onClick = onFinish) {
                Text("Finish")
            }
            Button(onClick = onRestart) {
                Text("Study Again")
            }
        }
    }
}

@Preview(name = "Card Front - Light", showBackground = true)
@Composable
fun PreviewCardFront() {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            StudyCardContent(
                card = createSampleCard(),
                isFlipped = false,
                progress = "5/20",
                onCardFlip = {},
                onGradeSelected = {}
            )
        }
    }
}

@Preview(name = "Card Front - Dark", showBackground = true)
@Composable
fun PreviewCardFrontDark() {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            StudyCardContent(
                card = createSampleCard(),
                isFlipped = false,
                progress = "5/20",
                onCardFlip = {},
                onGradeSelected = {}
            )
        }
    }
}

@Preview(name = "Card Back - Light", showBackground = true)
@Composable
fun PreviewCardBack() {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            StudyCardContent(
                card = createSampleCard(),
                isFlipped = true,
                progress = "5/20",
                onCardFlip = {},
                onGradeSelected = {}
            )
        }
    }
}

@Preview(name = "Card Back with Example", showBackground = true)
@Composable
fun PreviewCardBackWithExample() {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            StudyCardContent(
                card = createSampleCard(includeExample = true),
                isFlipped = true,
                progress = "5/20",
                onCardFlip = {},
                onGradeSelected = {}
            )
        }
    }
}

@Preview(name = "Study Finished", showBackground = true)
@Composable
fun PreviewStudyFinished() {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            StudyFinishedContent(
                onFinish = {},
                onRestart = {}
            )
        }
    }
}

@Preview(name = "Loading State", showBackground = true)
@Composable
fun PreviewLoadingState() {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Preview(
    name = "Interactive Card Flip Demo",
    showBackground = true,
    heightDp = 600
)
@Composable
fun PreviewInteractiveCardFlip() {
    MaterialTheme {
        var isFlipped by remember { mutableStateOf(false) }
        var currentCardIndex by remember { mutableStateOf(0) }

        val cards = listOf(
            createSampleCard(
                word = "Hello",
                translation = "Привет",
                transcription = "[həˈləʊ]"
            ),
            createSampleCard(
                word = "World",
                translation = "Мир",
                transcription = "[wɜːld]",
                example = "Hello world!",
                includeExample = true
            ),
            createSampleCard(
                word = "Beautiful",
                translation = "Красивый",
                transcription = "[ˈbjuːtɪfl]",
                example = "What a beautiful day!"
            )
        )

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Кнопки управления для демо
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = {
                            isFlipped = false
                            currentCardIndex = (currentCardIndex - 1 + cards.size) % cards.size
                        },
                        enabled = !isFlipped
                    ) {
                        Text("← Previous")
                    }

                    Button(
                        onClick = { isFlipped = !isFlipped }
                    ) {
                        Text(if (isFlipped) "Show Front" else "Show Back")
                    }

                    Button(
                        onClick = {
                            isFlipped = false
                            currentCardIndex = (currentCardIndex + 1) % cards.size
                        },
                        enabled = !isFlipped
                    ) {
                        Text("Next →")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Карточка
                StudyCardContent(
                    card = cards[currentCardIndex],
                    isFlipped = isFlipped,
                    progress = "${currentCardIndex + 1}/${cards.size}",
                    onCardFlip = { isFlipped = !isFlipped },
                    onGradeSelected = { grade ->
                        println("Selected grade: $grade")
                        // В демо просто переворачиваем обратно и переходим к следующей
                        isFlipped = false
                        currentCardIndex = (currentCardIndex + 1) % cards.size
                    }
                )
            }
        }
    }
}

private fun createSampleCard(
    word: String = "Hello",
    translation: String = "Привет",
    transcription: String? = "[həˈləʊ]",
    example: String? = null,
    includeExample: Boolean = false
): Card {
    return Card(
        id = 1,
        deckId = 1,
        word = word,
        translation = translation,
        example = example ?: if (includeExample) "Hello, how are you?" else null,
        transcription = transcription,
        easinessFactor = 2.5,
        interval = 0,
        repetitions = 0,
        nextReviewDate = null,
        createdAt = Instant.fromEpochSeconds(0),
        updatedAt = Instant.fromEpochSeconds(0)
    )
}