package com.example.pokedex.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.pokedex.ui.list.PokemonListScreen
import com.example.pokedex.ui.detail.PokemonDetailScreen
import com.example.pokedex.ui.detail.PokemonDetailViewModel
import com.example.pokedex.ui.list.PokemonListViewModel


@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "list") {
        composable("list") {
            val viewModel: PokemonListViewModel = viewModel() // Or your specific way of getting it
            PokemonListScreen(navController, viewModel)
        }
        composable("detail/{name}") { backStackEntry ->
//            val name = backStackEntry.arguments?.getString("name") ?: return@composable
//            val viewModel: PokemonDetailViewModel = viewModel()
//            PokemonDetailScreen(name, viewModel)

        }
    }
}
