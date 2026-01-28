package ru.otus.marketsample.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController

class NavigationState(
    val navHostController: NavHostController
) {

    fun navigateInBottomBarTo(route: Route) {
        navHostController.navigate(route) {
            popUpTo(navHostController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun navigateToProductDetails(id: String) {
        navHostController.navigate(ProductsRoute.Details(id))
    }
}

@Composable
fun rememberNavigationState(controller: NavHostController = rememberNavController()) =
    remember { NavigationState(controller) }