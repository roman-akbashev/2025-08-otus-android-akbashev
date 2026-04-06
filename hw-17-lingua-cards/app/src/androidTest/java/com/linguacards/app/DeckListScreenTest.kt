package com.linguacards.app

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import com.kaspersky.kaspresso.kaspresso.Kaspresso
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.linguacards.core.domain.repository.DeckRepository
import com.linguacards.features.decklist.R
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class DeckListScreenTest : TestCase(kaspressoBuilder = Kaspresso.Builder.simple()) {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    lateinit var context: Context

    @Inject
    lateinit var deckRepository: DeckRepository


    @Before
    fun setup() {
        hiltRule.inject()
        runTest {
            deckRepository.deleteAllDecks()
        }
        context = composeRule.activity.applicationContext
    }

    @Test
    fun testCreateDeck() = run {
        step("Open create deck dialog") {
            composeRule.onNodeWithTag("create_deck_fab").performClick()
        }

        step("Fill deck name and description") {
            composeRule.onNodeWithTag("deck_name").performTextInput("Test Deck")
            composeRule.onNodeWithTag("deck_description").performTextInput("Test Description")
        }

        step("Click create button") {
            composeRule.onNodeWithTag("create_button").performClick()
        }

        step("Verify deck is displayed") {
            composeRule.onNodeWithText("Test Deck").assertIsDisplayed()
            composeRule.onNodeWithText("Test Description").assertIsDisplayed()
            composeRule.onNodeWithText("0").assertIsDisplayed()
        }

        step("Verify deck stats") {
            composeRule.onNodeWithTag("deck_stats").assertIsDisplayed()
            composeRule.onNodeWithText(context.getString(R.string.deck_stats_total_decks))
                .assertIsDisplayed()
            composeRule.onNodeWithText("1").assertIsDisplayed()
        }
    }

    @Test
    fun testCreateDeckWithoutDescription() = run {
        step("Open create deck dialog") {
            composeRule.onNodeWithTag("create_deck_fab").performClick()
        }

        step("Fill only deck name") {
            composeRule.onNodeWithTag("deck_name").performTextInput("Simple Deck")
        }

        step("Click create button") {
            composeRule.onNodeWithTag("create_button").performClick()
        }

        step("Verify deck is displayed without description") {
            composeRule.onNodeWithText("Simple Deck").assertIsDisplayed()
            composeRule.onNodeWithText("0").assertIsDisplayed()
        }
    }

    @Test
    fun testSearchDeck() = run {
        step("Create test deck first") {
            composeRule.onNodeWithTag("create_deck_fab").performClick()
            composeRule.onNodeWithTag("deck_name").performTextInput("Search Test Deck")
            composeRule.onNodeWithTag("deck_description")
                .performTextInput("This is a test deck for search")
            composeRule.onNodeWithTag("create_button").performClick()
            composeRule.onNodeWithText("Search Test Deck").assertIsDisplayed()
        }

        step("Click search button") {
            composeRule.onNodeWithTag("search_button").performClick()
        }

        step("Enter search query") {
            composeRule.onNodeWithTag("search_field").performTextInput("Search Test")
        }

        step("Verify search results") {
            composeRule.onNodeWithText("Search Test Deck").assertIsDisplayed()
            composeRule.onNodeWithText("This is a test deck for search").assertIsDisplayed()
            composeRule.onNodeWithTag("deck_stats").assertIsDisplayed()
        }

        step("Clear search") {
            composeRule.onNodeWithTag("search_close").performClick()
            composeRule.onNodeWithText("Search Test Deck").assertIsDisplayed()
        }
    }

    @Test
    fun testDeleteDeck() = run {
        step("Create test deck") {
            composeRule.onNodeWithTag("create_deck_fab").performClick()
            composeRule.onNodeWithTag("deck_name").performTextInput("Deck To Delete")
            composeRule.onNodeWithTag("deck_description").performTextInput("Will be deleted")
            composeRule.onNodeWithTag("create_button").performClick()
            composeRule.onNodeWithText("Deck To Delete").assertIsDisplayed()
        }

        step("Long press on deck to open delete dialog") {
            composeRule.onNodeWithText("Deck To Delete").performTouchInput {
                longClick()
            }
        }

        step("Confirm deletion") {
            composeRule.onNodeWithTag("delete_dialog").assertIsDisplayed()
            composeRule.onNodeWithText(
                context.getString(
                    R.string.delete_deck_dialog_message,
                    "Deck To Delete"
                )
            ).assertIsDisplayed()
            composeRule.onNodeWithTag("delete_button").performClick()
        }

        step("Verify deck is deleted") {
            composeRule.onNodeWithText("Deck To Delete").assertDoesNotExist()
            composeRule.onNodeWithText(context.getString(R.string.empty_decks_title))
                .assertIsDisplayed()
        }
    }

    @Test
    fun testMultipleDecksCreation() = run {
        step("Create first deck") {
            composeRule.onNodeWithTag("create_deck_fab").performClick()
            composeRule.onNodeWithTag("deck_name").performTextInput("First Deck")
            composeRule.onNodeWithTag("deck_description").performTextInput("First description")
            composeRule.onNodeWithTag("create_button").performClick()
        }

        step("Create second deck") {
            composeRule.onNodeWithTag("create_deck_fab").performClick()
            composeRule.onNodeWithTag("deck_name").performTextInput("Second Deck")
            composeRule.onNodeWithTag("deck_description").performTextInput("Second description")
            composeRule.onNodeWithTag("create_button").performClick()
        }

        step("Verify both decks are displayed") {
            composeRule.onNodeWithText("First Deck").assertIsDisplayed()
            composeRule.onNodeWithText("Second Deck").assertIsDisplayed()
        }

        step("Verify deck stats show correct count") {
            composeRule.onNodeWithTag("deck_stats").assertIsDisplayed()
            composeRule.onNodeWithText("2").assertIsDisplayed()
            composeRule.onNodeWithText(context.getString(R.string.deck_stats_total_decks))
                .assertIsDisplayed()
        }
    }

}