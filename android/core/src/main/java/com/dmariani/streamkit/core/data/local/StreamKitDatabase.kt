package com.dmariani.streamkit.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Local Room database for StreamKit — currently holds the catalog cache
 * (`videos` table) used to render the Catalog screen offline-first.
 */
@Database(
    entities = [VideoEntity::class],
    version = 1,
    exportSchema = true
)
abstract class StreamKitDatabase : RoomDatabase() {
    abstract fun videoDao(): VideoDao
}
