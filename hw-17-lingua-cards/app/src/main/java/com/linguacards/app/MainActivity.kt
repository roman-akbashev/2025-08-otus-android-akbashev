package com.linguacards.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.linguacards.app.navigation.LinguaCardsNavHost
import com.linguacards.app.ui.theme.LinguaCardsTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            LinguaCardsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LinguaCardsApp()
                }
            }
        }
    }
}

@Composable
fun LinguaCardsApp(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    LinguaCardsNavHost(
        navController = navController,
        modifier = modifier
    )
}