package com.example.pokedex.ui.detail

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.rememberAsyncImagePainter

@Composable
fun PokemonDetailScreen(
    name: String,
    viewModel: PokemonDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Fetch when name changes
    LaunchedEffect(name) {
        viewModel.loadPokemon(name)
    }

    when (uiState) {
        is PokemonDetailUiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is PokemonDetailUiState.Error -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Error: ${(uiState as PokemonDetailUiState.Error).message}")
            }
        }

        is PokemonDetailUiState.Success -> {
            val pokemon = (uiState as PokemonDetailUiState.Success).pokemon
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = rememberAsyncImagePainter(pokemon.imageUrl),
                    contentDescription = null,
                    modifier = Modifier.size(128.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = pokemon.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Height: ${pokemon.height}")
                Text("Weight: ${pokemon.weight}")
            }
        }
    }
}
