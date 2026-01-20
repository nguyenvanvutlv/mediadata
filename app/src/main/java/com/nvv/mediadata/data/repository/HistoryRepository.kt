package com.nvv.mediadata.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.nvv.mediadata.data.db.HistoryDao
import com.nvv.mediadata.data.model.HistoryEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HistoryRepository @Inject constructor(
    private val historyDao: HistoryDao
) {
    fun getHistory(query: String): Flow<PagingData<HistoryEntity>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                if (query.isBlank()) historyDao.getAllHistory()
                else historyDao.searchHistory(query)
            }
        ).flow
    }

    suspend fun insertHistory(history: HistoryEntity) {
        historyDao.insertHistory(history)
    }

    suspend fun updateHistory(history: HistoryEntity) {
        historyDao.updateHistory(history)
    }

    suspend fun deleteHistory(history: HistoryEntity) {
        historyDao.deleteHistory(history)
    }

    suspend fun clearHistory() {
        historyDao.clearAllHistory()
    }
}
