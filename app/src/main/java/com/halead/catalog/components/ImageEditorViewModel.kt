package com.halead.catalog.components

import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.flow.StateFlow

interface ImageEditorViewModel {
    val polygonPoints: StateFlow<List<Offset>>

}