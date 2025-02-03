package com.halead.catalog.data

import com.halead.catalog.R
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataProvider @Inject constructor() {
    val materialsResIds = listOf<Int>(
        R.drawable.material,
        R.drawable.material1,
        R.drawable.material2,
        R.drawable.material3,
        R.drawable.material6,
        R.drawable.material4,
        R.drawable.material5,
    )
}
