package com.halead.catalog.repository.work

import android.content.Context
import com.halead.catalog.data.models.WorkModel
import com.halead.catalog.data.room.WorkDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class WorkRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    workDatabase: WorkDatabase
) : WorkRepository {
    private val workDao = workDatabase.workDao()

    override suspend fun getWorkHistory(): List<WorkModel> = coroutineScope {
//        return@coroutineScope workDao.getAll().mapNotNull { work ->
//            loadImageFromFile(work.baseImageFilePath)?.let { baseImage ->
//                WorkModel(
//                    id = work.id,
//                    baseImage = baseImage,
//                    overlays = work.overlays.mapNotNull { overlayData ->
//                        loadImageFromFile(overlayData.overlayFilePath)?.let { overlay ->
//                            OverlayMaterialModel(
//                                id = overlayData.id,
//                                overlay = overlay,
//                                material = overlayData.material,
//                                polygonPoints = overlayData.regionPoints,
//                                position = overlayData.position
//                            )
//                        }
//                    }
//                )
//            }
//        }
        return@coroutineScope emptyList<WorkModel>()
    }

    override suspend fun insert(workModel: WorkModel) {
//        coroutineScope {
//            launch(Dispatchers.IO) {
//                try {
//                    workDao.insert(
//                        WorkRoomEntity(
//                            id = workModel.id,
//                            baseImageFilePath = saveImageToFile(
//                                context,
//                                workModel.baseImage,
//                                "baseImage${workModel.id}"
//                            ),
//                            overlays = workModel.overlays.map { overlayData ->
//                                OverlayMaterialRoomEntity(
//                                    id = overlayData.id,
//                                    overlayFilePath = saveImageToFile(
//                                        context,
//                                        overlayData.overlay,
//                                        "overlayMaterialImage${overlayData.id}"
//                                    ),
//                                    material = overlayData.material,
//                                    regionPoints = overlayData.polygonPoints,
//                                    position = overlayData.position
//                                )
//                            }
//                        )
//                    )
//                } catch (e: Exception) {
//                    timber("InsertError", "Failed to insert workModel: ${e.message}")
//                }
//            }
//        }
    }

    override suspend fun update(workModel: WorkModel) {
//        coroutineScope {
//            launch(Dispatchers.IO) {
//                try {
//                    workDao.update(
//                        WorkRoomEntity(
//                            id = workModel.id,
//                            baseImageFilePath = saveImageToFile(
//                                context,
//                                workModel.baseImage,
//                                "baseImage${workModel.id}"
//                            ),
//                            overlays = workModel.overlays.map { overlayData ->
//                                OverlayMaterialRoomEntity(
//                                    id = overlayData.id,
//                                    overlayFilePath = saveImageToFile(
//                                        context,
//                                        overlayData.overlay,
//                                        "overlayMaterialImage${overlayData.id}"
//                                    ),
//                                    material = overlayData.material,
//                                    regionPoints = overlayData.polygonPoints,
//                                    position = overlayData.position
//                                )
//                            }
//                        )
//                    )
//                } catch (e: Exception) {
//                    timber("InsertError", "Failed to update workModel: ${e.message}")
//                }
//            }
//        }
    }

    override suspend fun delete(workModel: WorkModel) {
//        coroutineScope {
//            launch(Dispatchers.IO) {
//                try {
//                    workDao.deleteById(workModel.id)
//                } catch (e: Exception) {
//                    timber("InsertError", "Failed to delete workModel: ${e.message}")
//                }
//            }
//        }
    }
}
