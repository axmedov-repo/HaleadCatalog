package com.halead.catalog.data.states

import com.halead.catalog.data.models.WorkModel

data class WorkHistoryUIState(
    val works: List<WorkModel> = emptyList()
)
