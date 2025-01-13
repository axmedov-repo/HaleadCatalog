package com.halead.catalog.di

import androidx.room.Room
import com.halead.catalog.app.App
import com.halead.catalog.data.RecentActions
import com.halead.catalog.data.room.WorkDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ProviderModule {
    @Provides
    @Singleton
    fun getRecentActions(): RecentActions = RecentActions()

    @Provides
    @Singleton
    fun getWorkDatabase(): WorkDatabase = Room.databaseBuilder(
        App.instance,
        WorkDatabase::class.java, "work-database"
    ).build()

}