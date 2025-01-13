package com.halead.catalog.data

import androidx.compose.ui.geometry.Offset
import com.halead.catalog.data.models.OverlayMaterialModel
import com.halead.catalog.data.states.MainUiState
import com.halead.catalog.utils.UndoRedoManager
import javax.inject.Singleton

sealed interface RecentAction {
    //    data class UploadImage(val image: ImageBitmap) : RecentAction
    data class PolygonPoints(val polygonPoints: List<Offset>) : RecentAction
    data class OverlayMaterial(val overlayMaterial: OverlayMaterialModel) : RecentAction
    data class OverlayMaterialsList(val overlayMaterials: List<OverlayMaterialModel>) : RecentAction
}

@Singleton
class RecentActions {
    private val recentActions = UndoRedoManager<RecentAction>()

    /**
     * Only positive actions need to be recorded
     */
    fun act(action: RecentAction) {
        recentActions.addState(action)
    }

    fun undo(): RecentAction? {
        return recentActions.undo()
    }

    fun redo(): RecentAction? {
        return recentActions.redo()
    }

    fun canRedo() = recentActions.canRedo()

    fun canUndo() = recentActions.canUndo()

    fun clearAll() {
        recentActions.clearAll()
    }

    fun getUndoList() = recentActions.getUndoList()

    fun getRedoList() = recentActions.getRedoList()

    fun addAllUndoList(undoList: List<RecentAction>) {
        recentActions.addAllUndoList(undoList)
    }

    fun addAllRedoList(redoList: List<RecentAction>) {
        recentActions.addAllRedoList(redoList)
    }
}
