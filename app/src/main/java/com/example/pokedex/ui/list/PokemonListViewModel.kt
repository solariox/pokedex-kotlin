package com.example.pokedex.ui.list

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo.ApolloClient
import com.example.pokedex.GetPokemonDetailQuery
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

interface IPokemonListViewModel {
    val searchState: StateFlow<SearchState>
    val searchHistory: List<PokemonHistoryItem>
    fun checkPokemon(name: String, onResult: (exists: Boolean, pokemonName: String?) -> Unit)
    fun clearSearchHistory()
}


data class PokemonHistoryItem(
    val name: String,
    val spriteUrl: String
)

sealed interface SearchState {
    object Idle : SearchState
    object Loading : SearchState
    data class Success(val pokemonName: String, val exists: Boolean) : SearchState // Modifié pour inclure le nom
    data class Error(val message: String) : SearchState
}

@HiltViewModel
class PokemonListViewModel @Inject constructor(
    private val apolloClient: ApolloClient
) : ViewModel(), IPokemonListViewModel {

    private val _searchState = MutableStateFlow<SearchState>(SearchState.Idle)
    override val searchState: StateFlow<SearchState> = _searchState.asStateFlow()

    // Utiliser mutableStateListOf pour que les changements soient observés par Compose
    // si vous prévoyez d'afficher l'historique directement et qu'il change souvent.
    // Sinon, un StateFlow<List<PokemonHistoryItem>> avec une MutableList interne est aussi bien.
    private val _searchHistory = mutableStateListOf<PokemonHistoryItem>()
    override val searchHistory: List<PokemonHistoryItem> = _searchHistory

    companion object {
        private const val MAX_HISTORY_SIZE = 10
    }

    override fun checkPokemon(name: String, onResult: (exists: Boolean, pokemonName: String?) -> Unit) {
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

    private fun addPokemonToHistory(pokemon: GetPokemonDetailQuery.Pokemon) {
        val historyItem = PokemonHistoryItem(
            name = pokemon.name.orEmpty(),
            spriteUrl = pokemon.sprites?.front_default.orEmpty()
        )

        _searchHistory.removeAll { it.name.equals(historyItem.name, ignoreCase = true) }
        _searchHistory.add(0, historyItem)

        if (_searchHistory.size > MAX_HISTORY_SIZE) {
            _searchHistory.removeLastOrNull()
        }
    }

    override fun clearSearchHistory() {
        _searchHistory.clear()
    }
}

