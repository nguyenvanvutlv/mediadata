package com.nvv.mediadata.data.provide

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.nvv.mediadata.data.viewmodel.HistoryViewModel

@Composable
fun rememberHistoryViewModel(): HistoryViewModel {
	return hiltViewModel()
}
