package com.halead.catalog.screens

import android.net.Uri
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.ViewModel
import com.halead.catalog.app.App
import com.halead.catalog.data.RecentAction
import com.halead.catalog.data.RecentActions
import com.halead.catalog.data.entity.OverlayMaterial
import com.halead.catalog.data.enums.CursorData
import com.halead.catalog.data.enums.FunctionData
import com.halead.catalog.data.enums.FunctionsEnum
import com.halead.catalog.data.states.MainUiState
import com.halead.catalog.repository.MainRepository
import com.halead.catalog.utils.getBitmapFromUri
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class MainViewModelImpl @Inject constructor(
    private val mainRepository: MainRepository,
    private val recentActions: RecentActions
) : MainViewModel, ViewModel() {
    override val mainUiState = MutableStateFlow(MainUiState())

    init {
        getMaterials()
    }

    private fun getMaterials() {
        mainUiState.update {
            it.copy(materials = mainRepository.getMaterials())
        }
    }

    override fun selectMaterial(material: Int) {
        mainUiState.update {
            it.copy(selectedMaterial = material)
        }
    }

    override fun selectImage(uri: Uri?) {
        mainUiState.update {
            it.copy(imageBmp = getBitmapFromUri(App.instance, uri)?.asImageBitmap(), overlays = emptyList())
        }
    }

    override fun selectFunction(function: FunctionData) {
        when (function.type) {
            FunctionsEnum.UNDO -> {
                reAct(recentActions.undo(), true)
            }

            FunctionsEnum.REDO -> {
                reAct(recentActions.redo(), false)
            }

            else -> {}
        }
    }

    override fun selectCursor(cursorData: CursorData) {
        mainUiState.update { it.copy(currentCursor = cursorData) }
    }

    override fun addOverlay(overlayMaterial: OverlayMaterial) {
        recentActions.act(RecentAction.AddOverlayMaterial(overlayMaterial))
        mainUiState.update {
            it.copy(
                overlays = it.overlays.plus(overlayMaterial),
                canUndo = recentActions.canUndo(),
                canRedo = recentActions.canRedo()
            )
        }
    }

    private fun reAct(recentAction: RecentAction?, undo: Boolean) {
        when (recentAction) {
            is RecentAction.AddOverlayMaterial -> {
                if (undo) {
                    mainUiState.update {
                        it.copy(
                            overlays = it.overlays.minus(recentAction.overlayMaterial),
                            canUndo = recentActions.canUndo(),
                            canRedo = recentActions.canRedo()
                        )
                    }
                } else {
                    mainUiState.update {
                        it.copy(
                            overlays = it.overlays.plus(recentAction.overlayMaterial),
                            canUndo = recentActions.canUndo(),
                            canRedo = recentActions.canRedo()
                        )
                    }
                }
            }

            is RecentAction.DrawPoint -> {

            }

            null -> {}
        }
    }
}
