package com.halead.catalog.repository.work

import com.halead.catalog.data.models.WorkModel

interface WorkRepository {
    suspend fun getWorkHistory(): List<WorkModel>
    suspend fun insert(workModel: WorkModel)
    suspend fun update(workModel: WorkModel)
    suspend fun delete(workModel: WorkModel)
}