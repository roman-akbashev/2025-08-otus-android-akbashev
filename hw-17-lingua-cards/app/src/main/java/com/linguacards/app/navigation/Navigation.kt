package com.linguacards.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.linguacards.features.cardedit.presentation.CardEditScreen
import com.linguacards.features.deckdetail.presentation.DeckDetailScreen
import com.linguacards.features.decklist.presentation.DeckListScreen
import com.linguacards.features.study.presentation.StudyScreen

@Composable
fun LinguaCardsNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = "deck_list",
        modifier = modifier
    ) {
        // Главный экран - список колод
        composable("deck_list") {
            DeckListScreen(
                onDeckClick = { deckId ->
                    navController.navigate("deck_detail/$deckId")
                },
                onCreateDeck = {
                    navController.navigate("deck_edit/0")
                },
                onDeckLongPress = { deck ->
                    // Показать диалог с опциями
                }
            )
        }

        // Экран деталей колоды (список карточек)
        composable(
            "deck_detail/{deckId}",
            arguments = listOf(navArgument("deckId") { type = NavType.LongType })
        ) { backStackEntry ->
            val deckId = backStackEntry.arguments?.getLong("deckId") ?: 0L
            DeckDetailScreen(
                deckId = deckId,
                onCardClick = { cardId ->
                    navController.navigate("card_edit/$deckId/$cardId")
                },
                onAddCard = {
                    navController.navigate("card_edit/$deckId/0")
                },
                onStartStudy = {
                    navController.navigate("study/$deckId")
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        // Экран редактирования карточки
        composable(
            "card_edit/{deckId}/{cardId}",
            arguments = listOf(
                navArgument("deckId") { type = NavType.LongType },
                navArgument("cardId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val deckId = backStackEntry.arguments?.getLong("deckId") ?: 0L
            val cardId = backStackEntry.arguments?.getLong("cardId") ?: 0L
            CardEditScreen(
                onSave = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }

        // Экран изучения
        composable(
            "study/{deckId}",
            arguments = listOf(navArgument("deckId") { type = NavType.LongType })
        ) { backStackEntry ->
            StudyScreen(
                onFinish = { navController.popBackStack() }
            )
        }

        // Экран настроек
        composable("settings") {
            // SettingsScreen()
        }
    }
}