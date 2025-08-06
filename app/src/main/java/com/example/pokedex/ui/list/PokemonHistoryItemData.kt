package com.example.pokedex.ui.list

import kotlinx.serialization.Serializable

@Serializable
data class PokemonHistoryItemData(
    val name: String,
    val spriteUrl: String
)