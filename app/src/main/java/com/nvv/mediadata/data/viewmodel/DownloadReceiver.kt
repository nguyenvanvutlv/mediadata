package com.nvv.mediadata.data.viewmodel

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.nvv.mediadata.R

class DownloadReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == DownloadManager.ACTION_DOWNLOAD_COMPLETE) {
            val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (downloadId == -1L) return
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val query = DownloadManager.Query().setFilterById(downloadId)
            val cursor = downloadManager.query(query)
            if (cursor.moveToFirst()) {
                val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                val status = cursor.getInt(statusIndex)
                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                    val localUriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
                    val localUriString = cursor.getString(localUriIndex)
                    val titleIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TITLE)
                    val title = cursor.getString(titleIndex)
                    if (localUriString != null) {
                        enqueueFileProcessing(context, localUriString, title)
                    }
                } else if (status == DownloadManager.STATUS_FAILED) {
                    showNotification(
                        context,
                        context.getString(R.string.download_failed),
                        context.getString(R.string.error_occurred_during_download)
                    )
                }
            }
            cursor.close()
        }
    }

    private fun enqueueFileProcessing(context: Context, localUriString: String, title: String?) {
        val inputData = Data.Builder()
            .putString("EXTRA_LOCAL_URI", localUriString)
            .putString("EXTRA_TITLE", title)
            .build()
        val workRequest = OneTimeWorkRequestBuilder<FileProcessingWorker>()
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
    }

    private fun showNotification(context: Context, title: String, message: String) {
        val channelId = "download_status_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
	    val channel = NotificationChannel(
	        channelId,
	        context.getString(R.string.download_status_channel_name),
	        NotificationManager.IMPORTANCE_DEFAULT
	    )
	    notificationManager.createNotificationChannel(channel)
	    val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setAutoCancel(true)
            .build()
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
