package com.halead.catalog.di

import com.halead.catalog.data.RecentActions
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
}