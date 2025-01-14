package com.halead.catalog.data

import android.util.Log
import com.halead.catalog.data.states.MainUiState
import com.halead.catalog.utils.UndoRedoManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Singleton

sealed interface RecentAction {
    data class UiState(val mainUiState: MainUiState) : RecentAction
}

@Singleton
class RecentActions {
    private val recentActions = UndoRedoManager<RecentAction>()

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
        }
    }

    suspend fun undo(): RecentAction? = withContext(Dispatchers.IO) {
        val undoData = recentActions.undo()
        return@withContext undoData
    }

    suspend fun redo(): RecentAction? = withContext(Dispatchers.IO) {
        val redoData = recentActions.redo()
        Log.d("RecentActionsLog", "Returned with canUndo=${canUndo()}, canRedo=${canRedo()}")
        return@withContext redoData
    }

    fun canRedo() = recentActions.canRedo

    fun canUndo() = recentActions.canUndo

    suspend fun clearAll() = withContext(Dispatchers.IO) {
        recentActions.clearAll()
    }

    suspend fun getUndoList() = recentActions.undoStack

    suspend fun getRedoList() = recentActions.redoStack
}
