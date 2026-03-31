package com.linguacards.app

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
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
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

    @Inject
    lateinit var deckRepository: DeckRepository

    @Before
    fun setup() {
        hiltRule.inject()
        runBlocking {
            deckRepository.deleteAllDecks()
        }
    }

    @Test
    fun testCreateDeck() = run {
        step("Open create deck dialog") {
            composeRule.onNodeWithTag("create_deck_fab").performClick()
        }

        step("Fill deck name and description") {
            composeRule.onNodeWithText("Deck name").performTextInput("Test Deck")
            composeRule.onNodeWithText("Description (optional)")
                .performTextInput("Test Description")
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
            composeRule.onNodeWithText("Total Decks").assertIsDisplayed()
            composeRule.onNodeWithText("1").assertIsDisplayed()
        }
    }

    @Test
    fun testCreateDeckWithoutDescription() = run {
        step("Open create deck dialog") {
            composeRule.onNodeWithTag("create_deck_fab").performClick()
        }

        step("Fill only deck name") {
            composeRule.onNodeWithText("Deck name").performTextInput("Simple Deck")
        }

        step("Click create button") {
            composeRule.onNodeWithText("Create").performClick()
        }

        step("Verify deck is displayed without description") {
            composeRule.onNodeWithText("Simple Deck").assertIsDisplayed()
            composeRule.onNodeWithText("0").assertIsDisplayed()
        }
    }

    @Test
    fun testSearchDeck() = run {
        step("Create test deck first") {
            runBlocking {
                deckRepository.createDeck("Search Test Deck", "This is a test deck for search")
            }
            composeRule.waitForIdle()
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
    fun testSearchNoResults() = run {
        step("Create a deck") {
            runBlocking {
                deckRepository.createDeck("Regular Deck", "Regular description")
            }
            composeRule.waitForIdle()
        }

        step("Search for non-existent deck") {
            composeRule.onNodeWithTag("search_button").performClick()
            composeRule.onNodeWithTag("search_field").performTextInput("NonExistentDeck123")
        }

        step("Verify no results message") {
            composeRule.onNodeWithText("No results for \"NonExistentDeck123\"").assertIsDisplayed()
            composeRule.onNodeWithText("Try a different search term").assertIsDisplayed()
        }
    }

    @Test
    fun testDeleteDeck() = run {
        step("Create test deck") {
            runBlocking {
                deckRepository.createDeck("Deck To Delete", "Will be deleted")
            }
            composeRule.waitForIdle()
            composeRule.onNodeWithText("Deck To Delete").assertIsDisplayed()
        }

        step("Long press on deck to open delete dialog") {
            composeRule.onNodeWithText("Deck To Delete").performTouchInput {
                longClick()
            }
        }

        step("Confirm deletion") {
            composeRule.onNodeWithText("Delete Deck").assertIsDisplayed()
            composeRule.onNodeWithText("Are you sure you want to delete \"Deck To Delete\"? This will also delete all cards in this deck.")
                .assertIsDisplayed()
            composeRule.onNodeWithText("Delete").performClick()
        }

        step("Verify deck is deleted") {
            composeRule.onNodeWithText("Deck To Delete").assertDoesNotExist()
            composeRule.onNodeWithText("No decks yet").assertIsDisplayed()
        }
    }

    @Test
    fun testMultipleDecksCreation() = run {
        step("Create first deck") {
            runBlocking {
                deckRepository.createDeck("First Deck", "First description")
            }
            composeRule.waitForIdle()
        }

        step("Create second deck") {
            runBlocking {
                deckRepository.createDeck("Second Deck", "Second description")
            }
            composeRule.waitForIdle()
        }

        step("Verify both decks are displayed") {
            composeRule.onNodeWithText("First Deck").assertIsDisplayed()
            composeRule.onNodeWithText("Second Deck").assertIsDisplayed()
        }

        step("Verify deck stats show correct count") {
            composeRule.onNodeWithTag("deck_stats").assertIsDisplayed()
            composeRule.onNodeWithText("2").assertIsDisplayed()
            composeRule.onNodeWithText("Total Decks").assertIsDisplayed()
        }
    }

}