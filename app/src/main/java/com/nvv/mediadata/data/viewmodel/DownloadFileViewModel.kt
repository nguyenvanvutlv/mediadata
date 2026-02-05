package com.nvv.mediadata.data.viewmodel

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

	data class DownloadTask(
		val id: Int,
		val fileName: String,
		val progress: Int,
		val status: String,
	)

	private val _tasks = androidx.compose.runtime.mutableStateListOf<DownloadTask>()
	val tasks: List<DownloadTask> get() = _tasks

	fun startDownloadFile(link: String, title: String? = null) {
		val fileNameDisplay = if (!title.isNullOrBlank()) title else "Untitled_${System.currentTimeMillis()}"
		val tempFileName = "temp_${System.currentTimeMillis()}_${fileNameDisplay.hashCode()}"

		val dirPath = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.absolutePath
		if (dirPath == null) return

		val notificationId = System.currentTimeMillis().toInt()
		createNotificationChannel()
		showProgressNotification(notificationId, fileNameDisplay, 0)
		val taskIndex = _tasks.indexOfFirst { it.id == notificationId }
		val newTask = DownloadTask(notificationId, fileNameDisplay, 0, "Downloading")
		if (taskIndex == -1) {
			_tasks.add(newTask)
		} else {
			_tasks[taskIndex] = newTask
		}

		var lastUpdateTime = 0L
		var lastProgressPercent = -1

		com.downloader.PRDownloader.download(link, dirPath, tempFileName)
			.build()
			.setOnProgressListener { progress ->
				val currentTime = System.currentTimeMillis()
				val percent = if (progress.totalBytes > 0) {
					((progress.currentBytes * 100) / progress.totalBytes).toInt()
				} else {
					-1
				}

				if (currentTime - lastUpdateTime > 1000 || percent != lastProgressPercent) {
					if (currentTime - lastUpdateTime > 1000) {
						lastUpdateTime = currentTime
						lastProgressPercent = percent
						showProgressNotification(notificationId, fileNameDisplay, if (percent < 0) 0 else percent, percent < 0)
						val index = _tasks.indexOfFirst { it.id == notificationId }
						if (index != -1) {
							_tasks[index] = _tasks[index].copy(progress = if (percent < 0) 0 else percent)
						}
					}
				}
			}
			.start(object : com.downloader.OnDownloadListener {
				override fun onDownloadComplete() {
					val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
					notificationManager.cancel(notificationId)
					val index = _tasks.indexOfFirst { it.id == notificationId }
					if (index != -1) {
						_tasks[index] = _tasks[index].copy(progress = 100, status = "Success")
					}

					val file = java.io.File(dirPath, tempFileName)
					enqueueFileProcessing(file.absolutePath, fileNameDisplay)
				}

				override fun onError(error: com.downloader.Error?) {
					showErrorNotification(notificationId, fileNameDisplay)
					val index = _tasks.indexOfFirst { it.id == notificationId }
					if (index != -1) {
						_tasks[index] = _tasks[index].copy(status = "Failed")
					}
				}
			})
	}

	fun cancelDownload(id: Int) {
		com.downloader.PRDownloader.cancel(id)
		val index = _tasks.indexOfFirst { it.id == id }
		if (index != -1) {
			_tasks[index] = _tasks[index].copy(status = "Cancelled")

			val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
			notificationManager.cancel(id)
		}
	}

	private fun createNotificationChannel() {
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
			val channelId = "download_status_channel"
			val channel = android.app.NotificationChannel(
				channelId,
				context.getString(com.nvv.mediadata.R.string.download_status_channel_name),
				android.app.NotificationManager.IMPORTANCE_LOW
			)
			val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
			notificationManager.createNotificationChannel(channel)
		}
	}

	private fun showProgressNotification(notificationId: Int, title: String, progress: Int, indeterminate: Boolean = false) {
		val channelId = "download_status_channel"
		val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager

		val builder = androidx.core.app.NotificationCompat.Builder(context, channelId)
			.setContentTitle("Downloading $title")
			.setSmallIcon(android.R.drawable.stat_sys_download)
			.setOnlyAlertOnce(true)
			.setOngoing(true)

		if (indeterminate) {
			builder.setProgress(0, 0, true)
				.setContentText("Downloading...")
		} else {
			builder.setProgress(100, progress, false)
				.setContentText("$progress%")
		}

		notificationManager.notify(notificationId, builder.build())
	}

	private fun showErrorNotification(notificationId: Int, fileName: String) {
		val channelId = "download_status_channel"
		val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager

		val builder = androidx.core.app.NotificationCompat.Builder(context, channelId)
			.setContentTitle("Download Failed")
			.setContentText("Failed to download $fileName")
			.setSmallIcon(android.R.drawable.stat_notify_error)
			.setAutoCancel(true)

		notificationManager.notify(notificationId, builder.build())
	}

	private fun enqueueFileProcessing(localPath: String, title: String?) {
		val inputData = androidx.work.Data.Builder()
			.putString("EXTRA_LOCAL_URI", Uri.fromFile(java.io.File(localPath)).toString())
			.putString("EXTRA_TITLE", title)
			.build()

		val workRequest = androidx.work.OneTimeWorkRequestBuilder<FileProcessingWorker>()
			.setInputData(inputData)
			.build()

		androidx.work.WorkManager.getInstance(context).enqueue(workRequest)
	}
}