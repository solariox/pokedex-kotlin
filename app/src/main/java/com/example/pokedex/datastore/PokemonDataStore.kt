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
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.IOException

private val Context.pokemonHistoryDataStore: DataStore<Preferences> by preferencesDataStore(name = "pokemon_history_datastore")

private object PreferencesKeys {
    val POKEMON_SEARCH_HISTORY = stringPreferencesKey("pokemon_search_history_list")
}

// 3.
class PokemonDataStore(private val context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    // Flow pour exposer la liste de l'historique de recherche.
    val searchHistoryFlow: Flow<List<PokemonHistoryItemData>> = context.pokemonHistoryDataStore.data
        .catch { exception ->
            // dataStore.data lève une IOException si la lecture des données échoue.
            if (exception is IOException) {
                emit(emptyPreferences()) // Émettre des préférences vides en cas d'erreur de lecture.
            } else {
                throw exception // Relancer d'autres types d'exceptions.
            }
        }
        .map { preferences ->
            val jsonString = preferences[PreferencesKeys.POKEMON_SEARCH_HISTORY] ?: "[]" // Par défaut, une chaîne JSON de tableau vide.
            try {
                // Désérialiser la chaîne JSON en une List<PokemonHistoryItemData>.
                json.decodeFromString<List<PokemonHistoryItemData>>(jsonString)
            } catch (e: Exception) {
                // En cas d'erreur de désérialisation (par exemple, données corrompues),
                // retourner une liste vide et éventuellement logger l'erreur.
                // Log.e("PokemonDataStore", "Error deserializing search history", e)
                emptyList<PokemonHistoryItemData>()
            }
        }

    // Fonction suspendue pour sauvegarder la liste de l'historique de recherche.
    suspend fun saveSearchHistory(history: List<PokemonHistoryItemData>) {
        context.pokemonHistoryDataStore.edit { preferences ->
            // Sérialiser la liste en une chaîne JSON.
            val jsonString = json.encodeToString(history)
            preferences[PreferencesKeys.POKEMON_SEARCH_HISTORY] = jsonString
        }
    }

    // Fonction suspendue pour effacer l'historique de recherche.
    suspend fun clearSearchHistory() {
        context.pokemonHistoryDataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.POKEMON_SEARCH_HISTORY)
            // ou preferences[PreferencesKeys.POKEMON_SEARCH_HISTORY] = "[]" pour sauvegarder un tableau vide
        }
    }
}
