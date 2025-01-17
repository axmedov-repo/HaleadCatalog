package com.halead.catalog.data

import com.halead.catalog.data.states.MainUiState
import com.halead.catalog.utils.UndoRedoManager
import com.halead.catalog.utils.peekOrNull
import com.halead.catalog.utils.timber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

sealed interface RecentAction {
    data class UiState(val mainUiState: MainUiState) : RecentAction
}

@Singleton
class RecentActions @Inject constructor() {
    private val recentActions = UndoRedoManager<RecentAction>()
    private val mutex = kotlinx.coroutines.sync.Mutex()

    suspend fun act(action: RecentAction) {
        mutex.withLock {
            timber("RecentActionsLog", "Acting=$action")
            timber("RecentActionsLog", "undoStack.peek=${recentActions.undoStack.peekOrNull()}")
            if (recentActions.undoStack.peekOrNull() != action && recentActions.redoStack.peekOrNull() != action) {
                timber("RecentActionsLog", "Acted")
                recentActions.addState(action)
            }
        }
    }

    suspend fun undo(): RecentAction? {
        mutex.withLock {
            val undoData = recentActions.undo()
            return undoData
        }
    }

    suspend fun redo(): RecentAction? {
        mutex.withLock {
            val redoData = recentActions.redo()
            timber("RecentActionsLog", "Returned with canUndo=${canUndo()}, canRedo=${canRedo()}")
            return redoData
        }
    }

    fun canRedo() = recentActions.canRedo

    fun canUndo() = recentActions.canUndo

    suspend fun clearAll() = withContext(Dispatchers.IO) {
        recentActions.clearAll()
    }

    // Need for work history
    fun getUndoList() = recentActions.undoStack

    fun getRedoList() = recentActions.redoStack
}
