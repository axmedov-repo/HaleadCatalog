package com.halead.catalog.repository.work

import android.content.Context
import com.halead.catalog.utils.timber
import com.halead.catalog.app.App
import com.halead.catalog.data.models.OverlayMaterialModel
import com.halead.catalog.data.models.WorkModel
import com.halead.catalog.data.room.OverlayMaterialRoomEntity
import com.halead.catalog.data.room.WorkDatabase
import com.halead.catalog.data.room.WorkRoomEntity
import com.halead.catalog.utils.loadImageFromFile
import com.halead.catalog.utils.saveImageToFile
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class WorkRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    workDatabase: WorkDatabase
) : WorkRepository {
    private val workDao = workDatabase.workDao()

    override suspend fun getWorkHistory(): List<WorkModel> = coroutineScope {
        return@coroutineScope workDao.getAll().mapNotNull { work ->
            loadImageFromFile(work.baseImageFilePath)?.let { baseImage ->
                WorkModel(
                    id = work.id,
                    baseImage = baseImage,
                    overlays = work.overlays.mapNotNull { overlay ->
                        loadImageFromFile(overlay.materialFilePath)?.let { materialImage ->
                            OverlayMaterialModel(
                                id = overlay.id,
                                materialBitmap = materialImage,
                                regionPoints = overlay.regionPoints,
                                position = overlay.position
                            )
                        }
                    }
                )
            }
        }
    }

    override suspend fun insert(workModel: WorkModel) {
        coroutineScope {
            launch(Dispatchers.IO) {
                try {
                    workDao.insert(
                        WorkRoomEntity(
                            id = workModel.id,
                            baseImageFilePath = saveImageToFile(
                                context,
                                workModel.baseImage,
                                "baseImage${workModel.id}"
                            ),
                            overlays = workModel.overlays.map { overlay ->
                                OverlayMaterialRoomEntity(
                                    id = overlay.id,
                                    materialFilePath = saveImageToFile(
                                        context,
                                        overlay.materialBitmap,
                                        "overlayMaterialImage${overlay.id}"
                                    ),
                                    regionPoints = overlay.regionPoints,
                                    position = overlay.position
                                )
                            }
                        )
                    )
                } catch (e: Exception) {
                    timber("InsertError", "Failed to insert workModel: ${e.message}")
                }
            }
        }
    }

    override suspend fun update(workModel: WorkModel) {
        coroutineScope {
            launch(Dispatchers.IO) {
                try {
                    workDao.update(
                        WorkRoomEntity(
                            id = workModel.id,
                            baseImageFilePath = saveImageToFile(
                                context,
                                workModel.baseImage,
                                "baseImage${workModel.id}"
                            ),
                            overlays = workModel.overlays.map { overlay ->
                                OverlayMaterialRoomEntity(
                                    id = overlay.id,
                                    materialFilePath = saveImageToFile(
                                        context,
                                        overlay.materialBitmap,
                                        "overlayMaterialImage${overlay.id}"
                                    ),
                                    regionPoints = overlay.regionPoints,
                                    position = overlay.position
                                )
                            }
                        )
                    )
                } catch (e: Exception) {
                    timber("InsertError", "Failed to update workModel: ${e.message}")
                }
            }
        }
    }

    override suspend fun delete(workModel: WorkModel) {
        coroutineScope {
            launch(Dispatchers.IO) {
                try {
                    workDao.deleteById(workModel.id)
                } catch (e: Exception) {
                    timber("InsertError", "Failed to delete workModel: ${e.message}")
                }
            }
        }
    }
}
