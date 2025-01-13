package com.halead.catalog.utils

import java.util.Stack

class UndoRedoManager<T> {
    private val undoStack = Stack<T>()
    private val redoStack = Stack<T>()

    fun addState(state: T) {
        undoStack.push(state)
        redoStack.clear()
    }

    fun undo(): T? {
        if (undoStack.isNotEmpty()) {
            val temp = undoStack.pop()
            redoStack.push(temp)
            return temp
        }
        return null
    }

    fun redo(): T? {
        if (redoStack.isNotEmpty()) {
            val temp = redoStack.pop()
            undoStack.push(temp)
            return temp
        }
        return null
    }

    fun canUndo(): Boolean = undoStack.isNotEmpty()

    fun canRedo(): Boolean = redoStack.isNotEmpty()

    fun clearAll() {
        undoStack.clear()
        redoStack.clear()
    }

    fun getUndoList() = undoStack.toList()

    fun getRedoList() = redoStack.toList()

    fun addAllUndoList(undoList: List<T>) {
        undoStack.clear()
        undoStack.addAll(undoList)
    }

    fun addAllRedoList(redoList: List<T>) {
        redoStack.clear()
        redoStack.addAll(redoList)
    }
}
