package com.nvv.mediadata.data.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class DownloadFileViewModel @Inject constructor(
	@ApplicationContext private val context: Context,
) : ViewModel() {

	fun startDownloadFile(link: String, title: String? = null) {
		DownloadService.start(context, link, title)
	}
}