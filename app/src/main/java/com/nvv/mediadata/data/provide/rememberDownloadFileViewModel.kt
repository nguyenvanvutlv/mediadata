package com.nvv.mediadata.data.provide

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.nvv.mediadata.data.viewmodel.DownloadFileViewModel

@Composable
fun rememberDownloadFileViewModel() : DownloadFileViewModel{
	val context = rememberContext() as ComponentActivity
	return hiltViewModel(context)
}