package com.halead.catalog.di

import android.content.Context
import androidx.room.Room
import com.halead.catalog.data.room.WorkDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ProviderModule {
    @Provides
    @Singleton
    fun getWorkDatabase(@ApplicationContext context: Context): WorkDatabase = Room.databaseBuilder(
        context,
        WorkDatabase::class.java, "work-database"
    ).build() // Need to add migration if db scheme will be changed in the future.
}