package com.halead.catalog.data

import androidx.compose.ui.geometry.Offset
import com.halead.catalog.data.entity.OverlayMaterial
import com.halead.catalog.utils.UndoRedoManager

sealed interface RecentAction {
    //    data class UploadImage(val image: ImageBitmap) : RecentAction
    data class DrawPoint(val offset: Offset) : RecentAction
    data class AddOverlayMaterial(val overlayMaterial: OverlayMaterial) : RecentAction
}

class RecentActions {
    private val recentActions = UndoRedoManager<RecentAction>()

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
}
