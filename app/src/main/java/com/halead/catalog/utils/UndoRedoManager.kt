package com.halead.catalog.utils

import java.util.Stack

class UndoRedoManager<T> {
     val undoStack = Stack<T>()
     val redoStack = Stack<T>()

    var canUndo: Boolean = false
        private set
    var canRedo: Boolean = false
        private set

    fun addState(state: T) {
        undoStack.push(state)
        redoStack.clear() // Clear redo stack when a new state is added
        updateFlags()
    }

    fun undo(): T? {
        if (canUndo) {
            val currentState = undoStack.pop()
            redoStack.push(currentState)
            updateFlags()
            return if (canUndo) undoStack.peek() else null
        }
        return null
    }

    fun redo(): T? {
        if (canRedo) {
            val redoState = redoStack.pop()
            undoStack.push(redoState)
            updateFlags()
            return redoState
        }
        return null
    }

    private fun updateFlags() {
        canUndo = undoStack.isNotEmpty()
        canRedo = redoStack.isNotEmpty()
    }

    fun clearAll() {
        undoStack.clear()
        redoStack.clear()
    }

    fun addAllUndoList(undoList: List<T>) {
        undoStack.clear()
        undoStack.addAll(undoList)
    }

    fun addAllRedoList(redoList: List<T>) {
        redoStack.clear()
        redoStack.addAll(redoList)
    }
}
