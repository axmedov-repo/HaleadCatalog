package com.halead.catalog.data

import com.halead.catalog.R
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataProvider @Inject constructor() {
    val materials = mapOf(
        "Material 1" to R.drawable.material1,
        "Material 2" to R.drawable.material2,
        "Material 3" to R.drawable.material3
    )

    val materialsResIds = listOf<Int>(
        R.drawable.material,
        R.drawable.material1,
        R.drawable.material2,
        R.drawable.material3
    )
}
