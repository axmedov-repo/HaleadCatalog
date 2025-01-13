package com.halead.catalog.data.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface WorkDao {
    @Query("SELECT * FROM WorkRoomEntity")
    suspend fun getAll(): List<WorkRoomEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(workEntity: WorkRoomEntity)

    @Update
    suspend fun update(workEntity: WorkRoomEntity)

    @Delete
    suspend fun delete(workEntity: WorkRoomEntity)

    @Query("DELETE FROM WorkRoomEntity WHERE id = :id")
    suspend fun deleteById(id: Int)
}
