package ru.otus.common.data.promo

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

class PromoLocalDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    fun consumePromos(): Flow<List<PromoEntity>> = dataStore.data
        .map(::mapProductFromPrefs)

    suspend fun savePromos(products: List<PromoEntity>) {
        dataStore.edit { prefs -> prefs[productPreferencesKey] = encodeToString(products) }
    }

    @OptIn(InternalSerializationApi::class)
    private fun decodeFromString(string: String): List<PromoEntity> =
        try {
            Json.decodeFromString(ListSerializer(PromoEntity::class.serializer()), string)
        } catch (e: Exception) {
            listOf()
        }

    private fun mapProductFromPrefs(prefs: Preferences): List<PromoEntity> =
        prefs[productPreferencesKey]
            ?.takeIf(String::isNotEmpty)
            ?.let(this::decodeFromString) ?: listOf()

    private val productPreferencesKey = stringPreferencesKey(PRODUCT_KEY)

    @OptIn(InternalSerializationApi::class)
    private fun encodeToString(products: List<PromoEntity>): String =
        Json.encodeToString(
            ListSerializer(PromoEntity::class.serializer()),
            products,
        )

    private companion object {
        const val PRODUCT_KEY = "promo_key"
    }
}