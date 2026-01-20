package com.nvv.mediadata.data.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nvv.mediadata.data.model.HistoryEntity

@Dao
interface HistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: HistoryEntity)

    @Update
    suspend fun updateHistory(history: HistoryEntity)

    @Delete
    suspend fun deleteHistory(history: HistoryEntity)

    @Query("SELECT * FROM stream_history WHERE title LIKE '%' || :query || '%' OR url LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchHistory(query: String): PagingSource<Int, HistoryEntity>

    @Query("SELECT * FROM stream_history ORDER BY timestamp DESC")
    fun getAllHistory(): PagingSource<Int, HistoryEntity>

    @Query("DELETE FROM stream_history")
    suspend fun clearAllHistory()
}
