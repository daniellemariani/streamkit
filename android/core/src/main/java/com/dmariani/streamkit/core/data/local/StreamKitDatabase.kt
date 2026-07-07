package com.dmariani.streamkit.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [VideoEntity::class],
    version = 1,
    exportSchema = true
)
abstract class StreamKitDatabase : RoomDatabase() {
    abstract fun videoDao(): VideoDao
}
