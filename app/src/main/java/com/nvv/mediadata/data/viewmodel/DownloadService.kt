package com.nvv.mediadata.data.viewmodel

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.documentfile.provider.DocumentFile
import com.downloader.Error
import com.downloader.OnDownloadListener
import com.downloader.OnProgressListener
import com.downloader.PRDownloader
import com.downloader.Progress
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import androidx.core.net.toUri
import kotlin.math.min

class DownloadService : Service() {

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    private var downloadId: Int = -1
    private var lastPercent: Int = -1
    private var currentFileName: String = "Unknown File"

    companion object {
        private const val CHANNEL_ID = "download_channel"
        private const val NOTIFICATION_ID = 1001
        const val ACTION_START_DOWNLOAD = "ACTION_START_DOWNLOAD"
        const val EXTRA_URL = "EXTRA_URL"
        const val EXTRA_TITLE = "EXTRA_TITLE"

        fun start(context: Context, url: String, title: String? = null) {
            val intent = Intent(context, DownloadService::class.java).apply {
                action = ACTION_START_DOWNLOAD
                putExtra(EXTRA_URL, url)
                putExtra(EXTRA_TITLE, title)
            }
            context.startForegroundService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_START_DOWNLOAD) {
            val url = intent.getStringExtra(EXTRA_URL) ?: ""
            val title = intent.getStringExtra(EXTRA_TITLE)
            currentFileName = if (!title.isNullOrBlank()) {
                title
            } else {
                val fromUrl = url.substringAfterLast("/").substringBefore("?")
                if (fromUrl.isNotBlank() && fromUrl != "download") fromUrl else "Untitled_${System.currentTimeMillis() / 1000}"
            }

            startForeground(NOTIFICATION_ID, createNotification(0))
            processDownload(url, currentFileName)
        }
        return START_NOT_STICKY
    }

    private fun processDownload(url: String, title: String) {
        serviceScope.launch {
            val path = Settings.getPath(this@DownloadService)
            if (path.isEmpty()) {
                stopSelf()
                return@launch
            }

            val rawTitle = title
            
            val tempFileName = "temp_${System.currentTimeMillis()}.data"
            val tempFile = File(cacheDir, tempFileName)
            
            if (tempFile.exists()) tempFile.delete()

            downloadId = PRDownloader.download(url, tempFile.parent, tempFile.name)
                .build()
                .setOnProgressListener { progress ->
	                progress?.let {
		                if (it.totalBytes > 0) {
			                val percent = (it.currentBytes * 100 / it.totalBytes).toInt()
			                if (percent != lastPercent) {
				                lastPercent = percent
				                updateNotification(
									min(99, percent)
								)
			                }
		                }
	                }
                }
	            .start(object : OnDownloadListener {
                    override fun onDownloadComplete() {
                        serviceScope.launch {
                            val extension = detectVideoExtension(tempFile)
                            val safeTitle = rawTitle.replace("[^a-zA-Z0-9.\\- ]".toRegex(), "_")
                            val finalFileName = if (safeTitle.endsWith(extension, ignoreCase = true)) {
                                safeTitle
                            } else {
                                "$safeTitle$extension"
                            }
                            
                            val uri = path.toUri()
                            val targetDir = DocumentFile.fromTreeUri(this@DownloadService, uri)
                            
                            if (targetDir != null && targetDir.canWrite()) {
                                val success = copyFileStream(this@DownloadService, tempFile, targetDir, finalFileName)
                                if (success) {
                                    tempFile.delete()
                                    showCompleteNotification("Saved successfully: $finalFileName")
                                } else {
                                    showCompleteNotification("Error saving file to storage")
                                }
                            } else {
                                showCompleteNotification("Cannot write to selected folder")
                            }
                            stopForeground(STOP_FOREGROUND_DETACH)
                            stopSelf()
                        }
                    }

                    override fun onError(error: Error?) {
                        showCompleteNotification("Download error: ${error?.serverErrorMessage ?: "Unknown"}")
                        stopForeground(STOP_FOREGROUND_DETACH)
                        stopSelf()
                    }
                })
        }
    }

    private suspend fun copyFileStream(
        context: Context,
        sourceFile: File,
        targetDir: DocumentFile,
        targetName: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val mimeType = "video/*"
            val targetFile = targetDir.createFile(mimeType, targetName)
                ?: throw IOException("Can't create target file")
            context.contentResolver.openOutputStream(targetFile.uri)?.use { outputStream ->
                sourceFile.inputStream().use { inputStream ->
                    inputStream.copyTo(outputStream, bufferSize = 8 * 1024)
                }
            } ?: throw IOException("Can't open output stream")
            true
        } catch (e: Exception) {
            Timber.e(e, "Error copying file")
            false
        }
    }

    private fun detectVideoExtension(file: File): String {
        try {
            FileInputStream(file).use { inputStream ->
                val buffer = ByteArray(12)
                if (inputStream.read(buffer) != -1) {
                    val hexSignature = buffer.joinToString("") { "%02X".format(it) }
                    val asciiSignature = String(buffer)
                    if (hexSignature.startsWith("1A45DFA3")) return ".mkv"
                    if (asciiSignature.length >= 8 && asciiSignature.substring(4, 8) == "ftyp") return ".mp4"
                    if (hexSignature.startsWith("52494646")) return ".avi"
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error detecting extension")
        }
        return ".mp4"
    }

    private fun createNotificationChannel() {
	    val name = "Download Service"
	    val descriptionText = "Notifications for file downloads"
	    val importance = NotificationManager.IMPORTANCE_LOW
	    val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
	        description = descriptionText
	    }
	    val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
	    notificationManager.createNotificationChannel(channel)
    }

    private fun createNotification(progress: Int): android.app.Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Downloading: $currentFileName")
            .setContentText("Progress: $progress%")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setProgress(100, progress, false)
            .setOngoing(true)
            .build()
    }

    private fun showCompleteNotification(message: String) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Download Finished")
            .setContentText(message)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setProgress(0, 0, false)
            .setOngoing(false)
            .setAutoCancel(true)
            .build()
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun updateNotification(progress: Int) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, createNotification(progress))
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }
}
