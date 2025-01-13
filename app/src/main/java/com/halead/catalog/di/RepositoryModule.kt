package com.halead.catalog.di

import com.halead.catalog.repository.main.MainRepository
import com.halead.catalog.repository.main.MainRepositoryImpl
import com.halead.catalog.repository.work.WorkRepository
import com.halead.catalog.repository.work.WorkRepositoryImpl
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

    @Binds
    @Singleton
    fun getWorkRepository(workRepositoryImpl: WorkRepositoryImpl): WorkRepository
}
