package com.halead.catalog.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.halead.catalog.utils.Converters
import javax.inject.Singleton

@Singleton
@Database(entities = [WorkRoomEntity::class], version = 1)
@TypeConverters(Converters::class)
abstract class WorkDatabase : RoomDatabase() {
    abstract fun workDao(): WorkDao
}
