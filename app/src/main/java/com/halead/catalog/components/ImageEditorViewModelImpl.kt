package com.halead.catalog.components

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@HiltViewModel
class ImageEditorViewModelImpl @Inject constructor(

) : ImageEditorViewModel, ViewModel() {
    override val polygonPoints = MutableStateFlow<List<Offset>>(emptyList())


}
