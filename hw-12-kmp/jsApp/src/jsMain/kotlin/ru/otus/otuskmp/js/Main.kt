package ru.otus.otuskmp.js

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.example.otuskmp.StopwatchViewModel
import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.FlexDirection
import org.jetbrains.compose.web.css.JustifyContent
import org.jetbrains.compose.web.css.alignItems
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.flexDirection
import org.jetbrains.compose.web.css.fontFamily
import org.jetbrains.compose.web.css.gap
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.justifyContent
import org.jetbrains.compose.web.css.marginTop
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.vh
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H2
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.renderComposable

@Composable
fun StopWatchApp() {
    val viewModel = remember { StopwatchViewModel() }
    val uiState by viewModel.uiState.collectAsState()

    DisposableEffect(Unit) {
        onDispose {
            viewModel.onDestroy()
        }
    }

    Div(
        attrs = {
            style {
                display(DisplayStyle.Flex)
                flexDirection(FlexDirection.Column)
                alignItems(AlignItems.Center)
                justifyContent(JustifyContent.Center)
                height(100.vh)
                fontFamily("sans-serif")
            }
        }
    ) {
        H2 {
            Text(uiState.formattedTime)
        }

        Div(
            attrs = {
                style {
                    display(DisplayStyle.Flex)
                    gap(8.px)
                    marginTop(16.px)
                }
            }
        ) {
            Button(
                attrs = {
                    onClick { viewModel.onStartClicked() }
                }
            ) {
                Text("Start")
            }

            Button(
                attrs = {
                    onClick { viewModel.onStopClicked() }
                }
            ) {
                Text("Stop")
            }

            Button(
                attrs = {
                    onClick { viewModel.onCopyClicked() }
                }
            ) {
                Text("Copy")
            }
        }
    }
}

fun main() {
    renderComposable(rootElementId = "root") {
        StopWatchApp()
    }
}