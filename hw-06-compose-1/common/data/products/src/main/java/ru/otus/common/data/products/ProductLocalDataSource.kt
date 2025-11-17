package ru.otus.common.data.products

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import javax.inject.Inject

class ProductLocalDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    fun consumeProducts(): Flow<List<ProductEntity>> = dataStore.data
        .map(::mapProductFromPrefs)

    suspend fun saveProducts(products: List<ProductEntity>) {
        dataStore.edit { prefs -> prefs[productPreferencesKey] = encodeToString(products) }
    }

    @OptIn(InternalSerializationApi::class)
    private fun decodeFromString(string: String): List<ProductEntity> =
        try {
            Json.decodeFromString(ListSerializer(ProductEntity::class.serializer()), string)
        } catch (e: Exception) {
            listOf()
        }

    private fun mapProductFromPrefs(prefs: Preferences): List<ProductEntity> =
        prefs[productPreferencesKey]
            ?.takeIf(String::isNotEmpty)
            ?.let(this::decodeFromString) ?: listOf()

    private val productPreferencesKey = stringPreferencesKey(PRODUCT_KEY)

    @OptIn(InternalSerializationApi::class)
    private fun encodeToString(products: List<ProductEntity>): String =
        Json.encodeToString(
            ListSerializer(ProductEntity::class.serializer()),
            products,
        )

    private companion object {
        const val PRODUCT_KEY = "product_key"
    }
}