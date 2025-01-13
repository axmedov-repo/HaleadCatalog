package com.halead.catalog.screens.work

import com.halead.catalog.data.models.WorkModel
import com.halead.catalog.data.states.WorkHistoryUIState
import kotlinx.coroutines.flow.StateFlow

interface WorkViewModel {
    val uiState: StateFlow<WorkHistoryUIState>
    fun getWorkHistory()
    fun insertWork(workModel: WorkModel)
    fun updateWork(workModel: WorkModel)
    fun deleteWork(workModel: WorkModel)
}
