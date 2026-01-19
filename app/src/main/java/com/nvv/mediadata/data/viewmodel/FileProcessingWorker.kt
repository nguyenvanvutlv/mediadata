package com.nvv.mediadata.data.viewmodel

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.net.Uri
import android.os.Build
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

    override suspend fun doWork(): Result {
        val localUriString = inputData.getString("EXTRA_LOCAL_URI") ?: return Result.failure()
        val title = inputData.getString("EXTRA_TITLE") ?: "Unknown"

        return try {
            val sourceUri = localUriString.toUri()
            val path = Settings.getPath(applicationContext)
            if (path.isEmpty()) return Result.failure()

            val extension = applicationContext.contentResolver.openInputStream(sourceUri).use { input ->
                if (input != null) detectVideoExtensionFromStream(input) else ".mp4"
            }

            val safeTitle = title.replace("[\\\\/:*?\"<>|]".toRegex(), "_")
            val finalFileName = if (safeTitle.endsWith(extension, ignoreCase = true)) {
                safeTitle
            } else {
                "$safeTitle$extension"
            }

            val targetDirUri = path.toUri()
            val targetDir = DocumentFile.fromTreeUri(applicationContext, targetDirUri)

            if (targetDir != null && targetDir.canWrite()) {
                applicationContext.contentResolver.openInputStream(sourceUri).use { inputStream ->
                    if (inputStream == null) throw IOException("Can't open input stream")
                    val targetFile = targetDir.createFile("video/*", finalFileName)
                        ?: throw IOException("Can't create target file")
                    applicationContext.contentResolver.openOutputStream(targetFile.uri).use { outputStream ->
                        if (outputStream == null) throw IOException("Can't open output stream")
                        inputStream.copyTo(outputStream, bufferSize = 8 * 1024)
                    }
                }
                showNotification(
                    applicationContext.getString(R.string.download_finished),
                    applicationContext.getString(R.string.saved_successfully, finalFileName)
                )
                Result.success()
            } else {
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

    private fun detectVideoExtensionFromStream(inputStream: java.io.InputStream): String {
        try {
            val buffer = ByteArray(12)
            if (inputStream.read(buffer) != -1) {
                val hexSignature = buffer.joinToString("") { "%02X".format(it) }
                val asciiSignature = String(buffer)
                if (hexSignature.startsWith("1A45DFA3")) return ".mkv"
                if (asciiSignature.length >= 8 && asciiSignature.substring(4, 8) == "ftyp") return ".mp4"
                if (hexSignature.startsWith("52494646")) return ".avi"
            }
        } catch (e: Exception) {
            Timber.e(e, "Error detecting extension")
        }
        return ".mp4"
    }

    private fun showNotification(title: String, message: String) {
        val channelId = "download_status_channel"
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                applicationContext.getString(R.string.download_status_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
