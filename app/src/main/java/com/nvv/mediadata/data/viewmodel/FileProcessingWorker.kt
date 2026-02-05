package com.nvv.mediadata.data.viewmodel

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.nvv.mediadata.R
import timber.log.Timber
import java.io.IOException

class FileProcessingWorker(
	context: Context,
	workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

	override suspend fun doWork(): Result = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
		val localUriString = inputData.getString("EXTRA_LOCAL_URI") ?: return@withContext Result.failure()
		val title = inputData.getString("EXTRA_TITLE") ?: "Unknown"
		val mimeType = inputData.getString("EXTRA_MIME_TYPE")

		try {
			val sourceUri = localUriString.toUri()
			val path = Settings.getPath(applicationContext)
			if (path.isEmpty()) return@withContext Result.failure()

			val extension = if (mimeType != null) {
				android.webkit.MimeTypeMap.getSingleton()
					.getExtensionFromMimeType(mimeType)
					?.let { ".$it" }
			} else {
				null
			} ?: detectVideoExtensionFromStream(applicationContext, sourceUri)

			val safeTitle = title.replace("[\\\\/:*?\"<>|]".toRegex(), "_")
			val finalFileName = if (safeTitle.endsWith(extension, ignoreCase = true)) {
				safeTitle
			} else {
				"$safeTitle$extension"
			}

			val targetDirUri = path.toUri()
			val targetDir = DocumentFile.fromTreeUri(applicationContext, targetDirUri)

			if (targetDir != null && targetDir.canWrite()) {
				var finalFile = targetDir.findFile(finalFileName)
				var newFileName = finalFileName
				var counter = 1
				while (finalFile != null) {
					val nameWithoutExt = finalFileName.substringBeforeLast(".")
					newFileName = "$nameWithoutExt ($counter)$extension"
					finalFile = targetDir.findFile(newFileName)
					counter++
				}

				val targetFile = targetDir.createFile(mimeType ?: "video/*", newFileName)
					?: throw IOException("Can't create target file")

				applicationContext.contentResolver.openInputStream(sourceUri).use { inputStream ->
					if (inputStream == null) throw IOException("Can't open input stream")
					applicationContext.contentResolver.openOutputStream(targetFile.uri).use { outputStream ->
						if (outputStream == null) throw IOException("Can't open output stream")
						inputStream.copyTo(outputStream, bufferSize = 32 * 1024)
					}
				}

				showNotification(
					applicationContext.getString(R.string.download_finished),
					applicationContext.getString(R.string.saved_successfully, newFileName)
				)
				Result.success()
			} else {
				Timber.e("Target directory not writable or null: $targetDirUri")
				showNotification(
					applicationContext.getString(R.string.notification_title_error),
					applicationContext.getString(R.string.failed_to_save_downloaded_file)
				)
				Result.failure()
			}
		} catch (e: Exception) {
			Timber.e(e, "Error processing downloaded file")
			showNotification(
				applicationContext.getString(R.string.notification_title_error),
				applicationContext.getString(R.string.failed_to_save_downloaded_file)
			)
			Result.failure()
		}
	}

	private fun detectVideoExtensionFromStream(context: Context, uri: Uri): String {
		val retriever = android.media.MediaMetadataRetriever()
		return try {
			retriever.setDataSource(context, uri)
			val mimeType = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_MIMETYPE)
			if (mimeType != null) {
				android.webkit.MimeTypeMap.getSingleton()
					.getExtensionFromMimeType(mimeType)
					?.let { ".$it" } ?: ".mp4"
			} else {
				".mp4"
			}
		} catch (e: Exception) {
			Timber.e(e, "Error detecting extension with MediaMetadataRetriever")
			try {
				context.contentResolver.openInputStream(uri)?.use { input ->
					val buffer = ByteArray(12)
					if (input.read(buffer) != -1) {
						val hexSignature = buffer.joinToString(separator = "") { "%02X".format(it) }
						val asciiSignature = String(buffer)
						if (hexSignature.startsWith("1A45DFA3")) return ".mkv"
						if (asciiSignature.length >= 8 && asciiSignature.substring(4, 8) == "ftyp") return ".mp4"
						if (hexSignature.startsWith("52494646")) return ".avi"
					}
				}
			} catch (ignore: Exception) {
			}
			return ".mp4"
		} finally {
			try {
				retriever.release()
			} catch (ignore: Exception) {
			}
		}
	}

	private fun showNotification(title: String, message: String) {
		val channelId = "download_status_channel"
		val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

		val channel = NotificationChannel(
			channelId,
			applicationContext.getString(R.string.download_status_channel_name),
			NotificationManager.IMPORTANCE_DEFAULT
		)
		notificationManager.createNotificationChannel(channel)

		val notification = NotificationCompat.Builder(applicationContext, channelId)
			.setContentTitle(title)
			.setContentText(message)
			.setSmallIcon(android.R.drawable.stat_sys_download_done)
			.setAutoCancel(true)
			.build()

		notificationManager.notify(System.currentTimeMillis().toInt(), notification)
	}
}
