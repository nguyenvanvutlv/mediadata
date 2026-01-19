package com.nvv.mediadata.data.viewmodel

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class DownloadFileViewModel @Inject constructor(
	@ApplicationContext private val context: Context,
) : ViewModel() {

	fun startDownloadFile(link: String, title: String? = null) {
		val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
		
		val fileName = if (!title.isNullOrBlank()) {
			title
		} else {
			val fromUrl = link.substringAfterLast("/").substringBefore("?")
			if (fromUrl.isNotBlank() && fromUrl != "download") fromUrl else "Untitled_${System.currentTimeMillis() / 1000}"
		}

		val request = DownloadManager.Request(Uri.parse(link))
			.setTitle(fileName)
			.setDescription("Downloading video...")
			.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
			.setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, fileName)
			.setAllowedOverMetered(true)
			.setAllowedOverRoaming(true)

		downloadManager.enqueue(request)
	}
}