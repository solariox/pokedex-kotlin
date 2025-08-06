package com.example.pokedex.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo.ApolloClient
import com.example.pokedex.GetPokemonDetailQuery
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface PokemonDetailUiState {
    object Loading : PokemonDetailUiState
    data class Success(val pokemon: PokemonDetailUiModel) : PokemonDetailUiState
    data class Error(val message: String) : PokemonDetailUiState
}

data class PokemonDetailUiModel(
    val name: String,
    val height: Int,
    val weight: Int,
    val types: String,
    val imageUrl: String
)

@HiltViewModel
open class PokemonDetailViewModel @Inject constructor(
    private val apolloClient: ApolloClient
) : ViewModel() {

    private val _uiState = MutableStateFlow<PokemonDetailUiState>(PokemonDetailUiState.Loading)
    open val uiState: StateFlow<PokemonDetailUiState> = _uiState

    fun loadPokemon(name: String) {
        _uiState.value = PokemonDetailUiState.Loading

        viewModelScope.launch {
            try {
                val response = apolloClient.query(GetPokemonDetailQuery(name)).execute()
                val pokemon = response.data?.pokemon

                if (pokemon == null) {
                    _uiState.value = PokemonDetailUiState.Error("Pokémon not found.")
                    return@launch
                }

                val uiModel = PokemonDetailUiModel(
                    name = pokemon.name.orEmpty(),
                    height = pokemon.height ?: 0,
                    weight = pokemon.weight ?: 0,
                    types = pokemon.types
                        ?.mapNotNull { it?.type?.name }
                        ?.joinToString(", ")
                        .orEmpty(),

                    imageUrl = pokemon.sprites?.front_default.orEmpty()
                )

                _uiState.value = PokemonDetailUiState.Success(uiModel)
            } catch (e: Exception) {
                _uiState.value = PokemonDetailUiState.Error("API error: ${e.message}")
            }
        }
    }
}
