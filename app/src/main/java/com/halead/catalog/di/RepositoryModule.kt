package com.halead.catalog.di

import com.halead.catalog.repository.MainRepository
import com.halead.catalog.repository.MainRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {
    @Binds
    @Singleton
    fun getMainRepository(mainRepositoryImpl: MainRepositoryImpl): MainRepository
}