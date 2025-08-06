package com.example.pokedex.ui.list

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil3.compose.AsyncImage
import coil3.compose.rememberAsyncImagePainter
import com.example.pokedex.ui.theme.PokedexTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PokemonListScreen(
    navController: NavHostController,
    viewModel: IPokemonListViewModel = hiltViewModel<PokemonListViewModel>()
) {
    val scope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    val searchState by viewModel.searchState.collectAsState()
    val searchHistory = viewModel.searchHistory

    val keyboardController = LocalSoftwareKeyboardController.current
    val currentContext = LocalContext.current // Get the current context

    Scaffold() { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = "https://raw.githubusercontent.com/PokeAPI/sprites/refs/heads/master/sprites/pokemon/versions/generation-v/black-white/shiny//${Random.nextInt(151)}.png",
                contentDescription = "Pokédex",
                modifier = Modifier
                    .size(150.dp)
                    .padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search Pokémon by name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    if (searchQuery.isNotBlank()) {
                        viewModel.checkPokemon(searchQuery.trim()) { exists, pokemonName ->
                            if (exists) {
                                Toast.makeText(currentContext, "Pokémon \"$pokemonName\" found!", Toast.LENGTH_SHORT).show()
                            }
                        }
                        keyboardController?.hide()
                    }
                })
            )

            Spacer(modifier = Modifier.height(16.dp))

            when (val state = searchState) {
                is SearchState.Loading -> {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
                is SearchState.Error -> {
                    Text(
                        text = "Error: ${state.message}",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                is SearchState.Success -> {
                    if (!state.exists) {
                        Text(
                            text = "Pokémon \"${state.pokemonName}\" not found.",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {

                    }
                }
                is SearchState.Idle -> {
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (searchHistory.isNotEmpty()) {
                Text("Search History", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(searchHistory) { item ->
                        HistoryRow(item = item, onHistoryItemClick = {
                            navController.navigate("detail/${item.name.lowercase()}")
                        })
                        HorizontalDivider()
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { viewModel.clearSearchHistory() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Clear History")
                }
            } else {
                Box(modifier = Modifier.weight(1f)) {
                    Text(
                        "No search history yet.",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryRow(item: PokemonHistoryItem, onHistoryItemClick: (PokemonHistoryItem) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onHistoryItemClick(item) }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        item.spriteUrl.let { url ->
            Image(
                painter = rememberAsyncImagePainter(url),
                contentDescription = item.name,
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
            )
            Spacer(modifier = Modifier.width(12.dp))
        }
        Text(
            text = item.name.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }, // Capitalize
            style = MaterialTheme.typography.bodyLarge
        )
    }
}


private class FakePokemonListViewModel(
    initialSearchState: SearchState = SearchState.Idle,
    initialHistory: List<PokemonHistoryItem> = emptyList()
) : IPokemonListViewModel {
    override val searchState: MutableStateFlow<SearchState> = MutableStateFlow(initialSearchState)
    private val _searchHistory = mutableStateListOf<PokemonHistoryItem>().also { it.addAll(initialHistory) }
    override val searchHistory: List<PokemonHistoryItem> = _searchHistory

    override fun checkPokemon(name: String, onResult: (exists: Boolean, pokemonName: String?) -> Unit) {
        searchState.value = SearchState.Loading
        kotlinx.coroutines.MainScope().launch {
            kotlinx.coroutines.delay(1000)
            if (name.equals("pikachu", ignoreCase = true)) {
                val foundPokemon = PokemonHistoryItem("Pikachu", "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/25.png")
                _searchHistory.removeAll { it.name.equals(foundPokemon.name, ignoreCase = true) }
                _searchHistory.add(0, foundPokemon)
                searchState.value = SearchState.Success(foundPokemon.name, true)
                onResult(true, foundPokemon.name)
            } else if (name.equals("error", ignoreCase = true)) {
                searchState.value = SearchState.Error("Failed to fetch '$name'")
                onResult(false, name)
            } else {
                searchState.value = SearchState.Success(name, false)
                onResult(false, name)
            }
        }
    }

    override fun clearSearchHistory() {
        _searchHistory.clear()
    }
}

@Preview(showBackground = true, name = "Pokemon List - Idle")
@Composable
fun PokemonListScreenPreview_Idle() {
    PokedexTheme {
        PokemonListScreen(
            navController = rememberNavController(),
            viewModel = FakePokemonListViewModel(
                initialHistory = listOf(
                    PokemonHistoryItem("Bulbasaur", "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/1.png"),
                    PokemonHistoryItem("Charmander", "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/4.png")
                )
            )
        )
    }
}

@Preview(showBackground = true, name = "Pokemon List - Loading")
@Composable
fun PokemonListScreenPreview_Loading() {
    PokedexTheme {
        PokemonListScreen(
            navController = rememberNavController(),
            viewModel = FakePokemonListViewModel(initialSearchState = SearchState.Loading)
        )
    }
}

@Preview(showBackground = true, name = "Pokemon List - Error")
@Composable
fun PokemonListScreenPreview_Error() {
    PokedexTheme {
        PokemonListScreen(
            navController = rememberNavController(),
            viewModel = FakePokemonListViewModel(initialSearchState = SearchState.Error("Network connection lost"))
        )
    }
}

@Preview(showBackground = true, name = "Pokemon List - Not Found")
@Composable
fun PokemonListScreenPreview_NotFound() {
    PokedexTheme {
        PokemonListScreen(
            navController = rememberNavController(),
            viewModel = FakePokemonListViewModel(initialSearchState = SearchState.Success("NonExistentMon", false))
        )
    }
}

@Preview(showBackground = true, name = "Pokemon List - Empty History")
@Composable
fun PokemonListScreenPreview_EmptyHistory() {
    PokedexTheme {
        PokemonListScreen(
            navController = rememberNavController(),
            viewModel = FakePokemonListViewModel(initialHistory = emptyList())
        )
    }
}
