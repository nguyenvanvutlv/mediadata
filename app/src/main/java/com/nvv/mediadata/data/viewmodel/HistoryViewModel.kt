package com.nvv.mediadata.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.nvv.mediadata.data.model.HistoryEntity
import com.nvv.mediadata.data.repository.HistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HistoryViewModel @Inject constructor(
	private val repository: HistoryRepository
) : ViewModel() {

	private val _searchQuery = MutableStateFlow("")
	val searchQuery = _searchQuery.asStateFlow()

	val historyFlow: Flow<PagingData<HistoryEntity>> = _searchQuery
		.flatMapLatest { query ->
			repository.getHistory(query)
		}
		.cachedIn(viewModelScope)

	fun onSearchQueryChanged(query: String) {
		_searchQuery.value = query
	}

	fun insertHistory(title: String, url: String) {
		viewModelScope.launch {
			repository.insertHistory(HistoryEntity(title = title, url = url))
		}
	}

	fun updateHistory(history: HistoryEntity) {
		viewModelScope.launch {
			repository.updateHistory(history)
		}
	}

	fun deleteHistory(history: HistoryEntity) {
		viewModelScope.launch {
			repository.deleteHistory(history)
		}
	}

	fun clearAllHistory() {
		viewModelScope.launch {
			repository.clearHistory()
		}
	}
}
