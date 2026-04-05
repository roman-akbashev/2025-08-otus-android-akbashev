package com.linguacards.features.cardedit.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.linguacards.features.cardedit.R

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun CardEditScreen(
    onSave: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CardEditViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberScrollState()

    LaunchedEffect(state) {
        if (state is CardEditState.Saved) {
            onSave()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if ((state as? CardEditState.Content)?.isEditing == true)
                            stringResource(R.string.edit_card_title)
                        else
                            stringResource(R.string.new_card_title)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (viewModel.onSaveClick()) {
                                keyboardController?.hide()
                            }
                        },
                        modifier = Modifier.testTag("save_card_button")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Save")
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
            when (val currentState = state) {
                is CardEditState.Loading -> {
                    LoadingContent()
                }

                is CardEditState.Content -> {
                    CardEditForm(
                        state = currentState,
                        onWordChanged = viewModel::onWordChanged,
                        onTranslationChanged = viewModel::onTranslationChanged,
                        onExampleChanged = viewModel::onExampleChanged,
                        onTranscriptionChanged = viewModel::onTranscriptionChanged,
                        onWordFocusLost = viewModel::onWordFocusLost,
                        focusManager = focusManager,
                        viewModel = viewModel,
                        onCancel = onCancel,
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                    )
                }

                is CardEditState.Saved -> {
                    // Не отображаем, просто триггерим onSave
                }
            }
        }
    }
    if (errorMessage != null) {
        AlertDialog(
            onDismissRequest = {
                viewModel.clearErrorMessage()
            },
            title = { Text(stringResource(R.string.error_title)) },
            text = { Text(errorMessage ?: "Unknown error") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearErrorMessage()
                }) {
                    Text(stringResource(R.string.error_ok))
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardEditForm(
    state: CardEditState.Content,
    onWordChanged: (String) -> Unit,
    onTranslationChanged: (String) -> Unit,
    onExampleChanged: (String) -> Unit,
    onTranscriptionChanged: (String) -> Unit,
    onWordFocusLost: () -> Unit,
    focusManager: FocusManager,
    viewModel: CardEditViewModel,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var wordFocused by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Информационная карточка для существующих карточек
        if (state.isEditing && state.originalCard != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.statistics_title),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatChip(
                            label = stringResource(R.string.repetitions_label),
                            value = state.originalCard.repetitions.toString()
                        )
                        StatChip(
                            label = stringResource(R.string.interval_label),
                            value = "${state.originalCard.interval}"
                        )
                    }
                }
            }
        }

        // Поле для слова
        OutlinedTextField(
            value = state.word,
            onValueChange = onWordChanged,
            label = { Text(stringResource(R.string.word_label)) },
            placeholder = { Text(stringResource(R.string.word_placeholder)) },
            isError = state.errors.containsKey(ValidationErrorField.WORD),
            supportingText = {
                if (state.errors.containsKey(ValidationErrorField.WORD)) {
                    Text(
                        text = stringResource(R.string.word_required_error),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            leadingIcon = {
                Icon(Icons.Default.Translate, contentDescription = null)
            },
            trailingIcon = {
                if (state.isFetchingDetails) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                }
            },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("word_input")
                .onFocusChanged { focusState ->
                    if (!focusState.isFocused && wordFocused) {
                        // Потеря фокуса
                        onWordFocusLost()
                    }
                    wordFocused = focusState.isFocused
                },
            singleLine = true
        )

        // Поле для перевода
        OutlinedTextField(
            value = state.translation,
            onValueChange = onTranslationChanged,
            label = { Text(stringResource(R.string.translation_label)) },
            placeholder = { Text(stringResource(R.string.translation_placeholder)) },
            isError = state.errors.containsKey(ValidationErrorField.TRANSLATION),
            supportingText = {
                if (state.errors.containsKey(ValidationErrorField.TRANSLATION)) {
                    Text(
                        text = stringResource(R.string.translation_required_error),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            leadingIcon = {
                Icon(Icons.Default.Language, contentDescription = null)
            },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("translation_input"),
            singleLine = true
        )

        // Поле для транскрипции
        OutlinedTextField(
            value = state.transcription,
            onValueChange = onTranscriptionChanged,
            label = { Text(stringResource(R.string.transcription_label)) },
            placeholder = { Text(stringResource(R.string.transcription_placeholder)) },
            leadingIcon = {
                Icon(Icons.Default.Mic, contentDescription = null)
            },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Поле для примера
        OutlinedTextField(
            value = state.example,
            onValueChange = onExampleChanged,
            label = { Text(stringResource(R.string.example_label)) },
            placeholder = { Text(stringResource(R.string.example_placeholder)) },
            leadingIcon = {
                Icon(Icons.Default.Info, contentDescription = null)
            },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            ),
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            maxLines = 4
        )

        // Кнопки действий
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = {
                    if (state.isEditing) {
                        viewModel.resetState()
                    } else {
                        onCancel()
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    if (state.isEditing)
                        stringResource(R.string.reset_button)
                    else
                        stringResource(R.string.cancel_button)
                )
            }

            Button(
                onClick = {
                    if (viewModel.onSaveClick()) {
                        focusManager.clearFocus()
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = !state.isFetchingDetails
            ) {
                Text(
                    if (state.isEditing)
                        stringResource(R.string.update_button)
                    else
                        stringResource(R.string.create_button)
                )
            }
        }
    }
}

@Composable
fun StatChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
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
        Text(stringResource(R.string.loading_card))
    }
}