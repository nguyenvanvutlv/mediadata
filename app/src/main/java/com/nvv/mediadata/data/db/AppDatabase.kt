package com.nvv.mediadata.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.nvv.mediadata.data.model.HistoryEntity

@Database(entities = [HistoryEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
	abstract fun historyDao(): HistoryDao
}
