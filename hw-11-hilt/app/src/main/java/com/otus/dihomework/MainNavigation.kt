package com.otus.dihomework

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.otus.dihomework.common.di.findDependencies
import com.otus.dihomework.di.AppComponent
import com.otus.dihomework.features.favorites.ui.FavoritesScreenContent
import com.otus.dihomework.features.products.di.DaggerProductsComponent
import com.otus.dihomework.features.products.di.ProductsDependencies
import com.otus.dihomework.features.products.ui.ProductsScreenContent

sealed class Screen(val route: String, val titleRes: Int, val icon: ImageVector) {
    object Products : Screen("products", R.string.products_title, Icons.Default.List)
    object Favorites : Screen("favorites", R.string.favorites_title, Icons.Default.Favorite)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavigation() {
    val navController = rememberNavController()
    val screens = listOf(Screen.Products, Screen.Favorites)

    val context = LocalContext.current
    val appComponent: AppComponent = context.findDependencies()

    val viewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current) {
        "No ViewModelStoreOwner found"
    }

    Scaffold(
        topBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            val currentScreen = screens.find { screen ->
                currentDestination?.hierarchy?.any { it.route == screen.route } == true
            } ?: Screen.Products

            TopAppBar(
                title = { Text(stringResource(currentScreen.titleRes)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                screens.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = stringResource(screen.titleRes)) },
                        label = { Text(stringResource(screen.titleRes)) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Products.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Products.route) {
                val productsComponent = DaggerProductsComponent.factory()
                    .create(appComponent as ProductsDependencies)
                val viewModelFactory = productsComponent.viewModelFactory()
                val viewModel = ViewModelProvider(
                    viewModelStoreOwner,
                    viewModelFactory
                )[com.otus.dihomework.features.products.ProductsViewModel::class.java]

                ProductsScreenContent(viewModel = viewModel)
            }
            composable(Screen.Favorites.route) {
                val favoritesComponent = appComponent.favoritesComponent().create()
                val viewModelFactory = favoritesComponent.viewModelFactory()
                val viewModel = ViewModelProvider(
                    viewModelStoreOwner,
                    viewModelFactory
                )[com.otus.dihomework.features.favorites.FavoritesViewModel::class.java]

                FavoritesScreenContent(viewModel = viewModel)
            }
        }
    }
}