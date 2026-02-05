package com.otus.dihomework.common.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.otus.dihomework.common.domain_impl.FavoritesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "favorites")

class FavoritesRepositoryImpl(
    private val context: Context
) : FavoritesRepository {

    private val favoritesKey = stringSetPreferencesKey("favorite_product_ids")

    override fun consumeFavoriteIds(): Flow<Set<String>> {
        return context.dataStore.data.map { preferences ->
            preferences[favoritesKey] ?: emptySet()
        }
    }

    override suspend fun addToFavorites(productId: String) {
        context.dataStore.edit { preferences ->
            val currentFavorites = preferences[favoritesKey] ?: emptySet()
            preferences[favoritesKey] = currentFavorites + productId
        }
    }

    override suspend fun removeFromFavorites(productId: String) {
        context.dataStore.edit { preferences ->
            val currentFavorites = preferences[favoritesKey] ?: emptySet()
            preferences[favoritesKey] = currentFavorites - productId
        }
    }
}
