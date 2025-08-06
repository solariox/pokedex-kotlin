package com.example.pokedex.ui.detail

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.rememberAsyncImagePainter

@Composable
fun PokemonDetailScreen(
    name: String,
    viewModel: PokemonDetailViewModel = hiltViewModel()
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



//@Composable
//@Preview(showBackground = true)
//fun PokemonDetailPreview() {
//    val fakeUiState = remember {
//        mutableStateOf<PokemonDetailUiState>(
//            PokemonDetailUiState.Success(
//                pokemon = PokemonDetailUiModel(
//                    name = "Pikachu",
//                    height = 40,
//                    weight = 60,
//                    imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/25.png"
//                )
//            )
//        )
//    }
//
//    val ApolloClient = ApolloModule.provideApolloClient()
//
//    val fakeViewModel = object : PokemonDetailViewModel(ApolloClient) {
//        override val uiState: StateFlow<PokemonDetailUiState>
//            get() = MutableStateFlow(fakeUiState.value)
//    }
//
//    PokedexTheme {
//        PokemonDetailScreen(name = "Pikachu", fakeViewModel)
//    }
//}
