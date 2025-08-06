package com.example.pokedex.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.pokedex.ui.list.PokemonHistoryItemData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import java.io.IOException

private val Context.pokemonHistoryDataStore: DataStore<Preferences> by preferencesDataStore(name = "pokemon_history_datastore")

private object PreferencesKeys {
    val POKEMON_SEARCH_HISTORY = stringPreferencesKey("pokemon_search_history_list")
}

class PokemonDataStore(private val context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    val searchHistoryFlow: Flow<List<PokemonHistoryItemData>> = context.pokemonHistoryDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val jsonString = preferences[PreferencesKeys.POKEMON_SEARCH_HISTORY] ?: "[]"
            try {
                json.decodeFromString<List<PokemonHistoryItemData>>(jsonString)
            } catch (e: Exception) {
                emptyList<PokemonHistoryItemData>()
            }
        }

    suspend fun saveSearchHistory(history: List<PokemonHistoryItemData>) {
        context.pokemonHistoryDataStore.edit { preferences ->
            val jsonString = json.encodeToString(history)
            preferences[PreferencesKeys.POKEMON_SEARCH_HISTORY] = jsonString
        }
    }

    suspend fun clearSearchHistory() {
        context.pokemonHistoryDataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.POKEMON_SEARCH_HISTORY)
        }
    }
}
