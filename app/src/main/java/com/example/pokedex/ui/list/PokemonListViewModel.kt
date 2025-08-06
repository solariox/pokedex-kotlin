package com.example.pokedex.ui.list

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo.ApolloClient
import com.example.pokedex.GetPokemonDetailQuery
import com.example.pokedex.datastore.PokemonDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

interface IPokemonListViewModel {
    val searchState: StateFlow<SearchState>
    val searchHistory: List<PokemonHistoryItemData>
    fun checkPokemon(name: String, onResult: (exists: Boolean, pokemonName: String?) -> Unit)
    fun clearSearchHistory()
}


sealed interface SearchState {
    object Idle : SearchState
    object Loading : SearchState
    data class Success(val pokemonName: String, val exists: Boolean) :
        SearchState // Modifié pour inclure le nom

    data class Error(val message: String) : SearchState
}

@HiltViewModel
class PokemonListViewModel @Inject constructor(
    private val apolloClient: ApolloClient,
    private val pokemonDataStore: PokemonDataStore
) : ViewModel(), IPokemonListViewModel {

    private val _searchState = MutableStateFlow<SearchState>(SearchState.Idle)
    override val searchState: StateFlow<SearchState> = _searchState.asStateFlow()

    private val _searchHistory = mutableStateListOf<PokemonHistoryItemData>()
    override val searchHistory: List<PokemonHistoryItemData> = _searchHistory

    companion object {
        private const val MAX_HISTORY_SIZE = 10
    }
    init {
        viewModelScope.launch {
            pokemonDataStore.searchHistoryFlow
                .distinctUntilChanged() // Optimisation pour éviter les mises à jour inutiles
                .collect { historyFromDataStore ->
                    _searchHistory.clear()
                    // Prendre seulement les MAX_HISTORY_SIZE plus récents si nécessaire lors du chargement
                    _searchHistory.addAll(historyFromDataStore.take(MAX_HISTORY_SIZE))
                }
        }
    }

    override fun checkPokemon(
        name: String,
        onResult: (exists: Boolean, pokemonName: String?) -> Unit
    ) {
        val queryName = name.trim().lowercase()
        if (queryName.isEmpty()) {
            _searchState.value = SearchState.Error("Search query cannot be empty.")
            onResult(false, null)
            return
        }

        _searchState.value = SearchState.Loading
        viewModelScope.launch {
            try {
                Log.d("PokemonListViewModel", "Checking for Pokémon: $queryName")
                val response = apolloClient.query(GetPokemonDetailQuery(queryName)).execute()

                if (response.hasErrors()) {
                    val errorMessage = response.errors?.firstOrNull()?.message ?: "Unknown error"
                    Log.e("PokemonListViewModel", "GraphQL Error: $errorMessage")
                    _searchState.value = SearchState.Error(errorMessage)
                    onResult(false, null)
                    return@launch
                }

                val pokemon = response.data?.pokemon
                val exists = pokemon != null

                if (exists && pokemon != null) {
                    _searchState.value = SearchState.Success(pokemon.name.orEmpty(), true)
                    addPokemonToHistory(pokemon)
                    onResult(true, pokemon.name)
                } else {
                    _searchState.value = SearchState.Success(queryName, false)
                    onResult(false, null)
                }

            } catch (e: Exception) {
                Log.e("PokemonListViewModel", "API Error: ${e.message}", e)
                _searchState.value = SearchState.Error("API Error: ${e.message}")
                onResult(false, null)
            }
        }
    }

    private fun addPokemonToHistory(pokemon: GetPokemonDetailQuery.Pokemon) { // Utilisez votre type Apollo ici
        val newItem = PokemonHistoryItemData(
            name = pokemon.name.orEmpty(),
            spriteUrl = pokemon.sprites?.front_default.orEmpty()
        )

        _searchHistory.removeAll { it.name.equals(newItem.name, ignoreCase = true) }
        _searchHistory.add(0, newItem) // Ajouter au début pour le plus récent
        if (_searchHistory.size > MAX_HISTORY_SIZE) {
            _searchHistory.removeLastOrNull() // Enlever le plus ancien si la limite est dépassée
        }

        viewModelScope.launch {
            pokemonDataStore.saveSearchHistory(_searchHistory.toList()) // Convertir en List immuable
        }
    }

    override fun clearSearchHistory() {
        _searchHistory.clear()
        viewModelScope.launch {
            pokemonDataStore.clearSearchHistory()
        }
    }
}

