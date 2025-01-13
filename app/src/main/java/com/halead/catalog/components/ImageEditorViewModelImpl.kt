package com.halead.catalog.components

import android.util.Log
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ImageEditorViewModelImpl @Inject constructor(

) : ImageEditorViewModel, ViewModel() {
    override val polygonPoints = MutableStateFlow<List<Offset>>(emptyList())


}
