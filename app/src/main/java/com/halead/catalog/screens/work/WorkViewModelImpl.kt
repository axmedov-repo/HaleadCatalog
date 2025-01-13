package com.halead.catalog.screens.work

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.halead.catalog.data.models.WorkModel
import com.halead.catalog.data.states.WorkHistoryUIState
import com.halead.catalog.repository.work.WorkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkViewModelImpl @Inject constructor(
    private val workRepository: WorkRepository
) : WorkViewModel, ViewModel() {
    override val uiState = MutableStateFlow(WorkHistoryUIState())

    init {
        getWorkHistory()
    }

    override fun getWorkHistory() {
        viewModelScope.launch {
            uiState.update {
                it.copy(works = workRepository.getWorkHistory())
            }
        }
    }

    override fun insertWork(workModel: WorkModel) {
        viewModelScope.launch {
            workRepository.insert(workModel)
        }
    }

    override fun updateWork(workModel: WorkModel) {
        viewModelScope.launch {
            workRepository.update(workModel)
        }
    }

    override fun deleteWork(workModel: WorkModel) {
        viewModelScope.launch {
            workRepository.delete(workModel)
        }
    }
}