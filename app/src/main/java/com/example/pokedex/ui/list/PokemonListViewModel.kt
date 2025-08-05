package com.example.pokedex.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo.ApolloClient
import com.example.pokedex.GetPokemonDetailQuery
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface SearchState {
    object Idle : SearchState
    object Loading : SearchState
    object Success : SearchState
    data class Error(val message: String) : SearchState
}

class PokemonListViewModel(
    private val apolloClient: ApolloClient = ApolloClient.Builder()
        .serverUrl("https://graphql-pokeapi.graphcdn.app/")
        .build()
) : ViewModel() {

    private val _searchState = MutableStateFlow<SearchState>(SearchState.Idle)
    val searchState: StateFlow<SearchState> = _searchState

    fun checkPokemon(name: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _searchState.value = SearchState.Loading
            try {
                val response = apolloClient.query(GetPokemonDetailQuery(name)).execute()
                val exists = response.data?.pokemon?.name != null
                _searchState.value = if (exists) SearchState.Success else SearchState.Error("Not found")
                onResult(exists)
            } catch (e: Exception) {
                _searchState.value = SearchState.Error("API Error: ${e.message}")
                onResult(false)
            }
        }
    }
}
