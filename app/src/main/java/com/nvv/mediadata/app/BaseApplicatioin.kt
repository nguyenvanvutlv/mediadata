package com.nvv.mediadata.app

import android.app.Application
import androidx.annotation.OptIn
import com.downloader.PRDownloader
import com.downloader.PRDownloaderConfig
import com.nvv.mediadata.BuildConfig
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.UnstableApi
import timber.log.Timber

@OptIn(UnstableApi::class)
@HiltAndroidApp
class BaseApplication : Application() {
	override fun onCreate() {
		super.onCreate()
		/// create logger timber
		if (BuildConfig.DEBUG) {
			Timber.plant(Timber.DebugTree())
		}
		// Initialize PRDownloader
		val config = PRDownloaderConfig.newBuilder()
			.setDatabaseEnabled(true)
			.build()
		PRDownloader.initialize(applicationContext, config)
	}
}