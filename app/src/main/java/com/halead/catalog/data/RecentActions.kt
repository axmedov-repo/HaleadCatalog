package com.halead.catalog.data

import android.util.Log
import com.halead.catalog.data.states.MainUiState
import com.halead.catalog.utils.UndoRedoManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Singleton

sealed interface RecentAction {
    data class UiState(val mainUiState: MainUiState) : RecentAction
//    data class UploadImage(val image: ImageBitmap) : RecentAction
//    data class PolygonPoints(val polygonPoints: List<Offset>) : RecentAction
//    data class OverlayMaterial(val overlayMaterial: OverlayMaterialModel) : RecentAction
//    data class OverlayMaterialsList(val overlayMaterials: List<OverlayMaterialModel>) : RecentAction
}

@Singleton
class RecentActions {
    private val recentActions = UndoRedoManager<RecentAction>()

    private var onRecentActionsChanged: suspend () -> Unit = {}

    suspend fun setOnRecentActionsChanged(listener: suspend () -> Unit) {
        onRecentActionsChanged = listener
    }

    suspend fun act(action: RecentAction) = withContext(Dispatchers.IO) {
        Log.d("RecentActionsLog", "Acting=$action")
        Log.d("RecentActionsLog", "isUndoStackEmpty=${recentActions.undoStack.isEmpty()}")
        Log.d(
            "RecentActionsLog",
            "undoStack.peek=${if (!recentActions.undoStack.isEmpty()) recentActions.undoStack.peek() else ""}"
        )
        if (recentActions.undoStack.isEmpty() || recentActions.undoStack.peek() != action) {
            Log.d("RecentActionsLog", "Acted")
            recentActions.addState(action)
            onRecentActionsChanged()
        }
    }

    suspend fun undo(): RecentAction? = withContext(Dispatchers.IO) {
        val undoData = recentActions.undo()
        undoData?.let {
            // Delay the trigger to ensure data is returned first
            withContext(Dispatchers.Main) {
                onRecentActionsChanged()
            }
        }
        return@withContext undoData
    }

    suspend fun redo(): RecentAction? = withContext(Dispatchers.IO) {
        val redoData = recentActions.redo()
        redoData?.let {
            withContext(Dispatchers.Main) {
                Log.d("RecentActionsLog", "Calling Listener with canUndo=${canUndo()}, canRedo=${canRedo()}")
                onRecentActionsChanged()
            }
        }
        Log.d("RecentActionsLog", "Returned with canUndo=${canUndo()}, canRedo=${canRedo()}")
        return@withContext redoData
    }

    fun canRedo() = recentActions.canRedo

    fun canUndo() = recentActions.canUndo

    suspend fun clearAll() = withContext(Dispatchers.IO) {
        recentActions.clearAll()
        onRecentActionsChanged()
    }

    suspend fun getUndoList() = recentActions.undoStack

    suspend fun getRedoList() = recentActions.redoStack
}
