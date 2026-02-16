package ru.otus.marketsample

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import ru.otus.marketsample.navigation.BottomBar
import ru.otus.marketsample.navigation.MarketNavigationGraph
import ru.otus.marketsample.navigation.rememberNavigationState

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navigationState = rememberNavigationState()

            Scaffold(
                bottomBar = {
                    BottomBar(navigationState)
                }
            ) {
                MarketNavigationGraph(navigationState, Modifier.padding(it))
            }
        }
    }
}