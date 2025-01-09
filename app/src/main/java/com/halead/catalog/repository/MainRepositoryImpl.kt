package com.halead.catalog.repository

import com.halead.catalog.data.DataProvider
import javax.inject.Inject

class MainRepositoryImpl @Inject constructor(

) : MainRepository {
    override fun getMaterials(): Map<String, Int> = DataProvider.materials
}