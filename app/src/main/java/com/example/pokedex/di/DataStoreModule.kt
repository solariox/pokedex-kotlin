package com.example.pokedex.di

import android.content.Context
import com.example.pokedex.datastore.PokemonDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Singleton
    @Provides
    fun providePokemonDataStore(@ApplicationContext context: Context): PokemonDataStore {
        return PokemonDataStore(context)
    }
}