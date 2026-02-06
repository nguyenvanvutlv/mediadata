package com.nvv.mediadata.app

import android.app.Application
import android.app.UiModeManager
import android.content.res.Configuration
import androidx.media3.common.util.Log
import com.nvv.mediadata.BuildConfig
import com.nvv.mediadata.data.connecttv.TvConnectManager
import com.nvv.mediadata.data.connecttv.TvPlayRequestCallback
import com.nvv.mediadata.data.connecttv.TvPlayRequestStore
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber


@androidx.media3.common.util.UnstableApi
@HiltAndroidApp
class BaseApplication : Application() {
	private var tvConnectManager: TvConnectManager? = null


	override fun onCreate() {
		super.onCreate()
		if (BuildConfig.DEBUG) {
			Timber.plant(Timber.DebugTree())
			Log.setLogLevel(Log.LOG_LEVEL_ALL)
		} else {
			Log.setLogLevel(Log.LOG_LEVEL_OFF)
		}
		if (isRunningOnTv()) {
			val callback = TvPlayRequestCallback { url ->
				Timber.i("TvConnectManager: received remote play request url=%s", url)
				TvPlayRequestStore.onRemotePlayRequested(url)
			}
			tvConnectManager = TvConnectManager(
				application = this,
				playRequestCallback = callback,
			).also { manager ->
				manager.attachToProcessLifecycle()
			}
		}
	}

	private fun isRunningOnTv(): Boolean {
		val uiModeManager = getSystemService(UI_MODE_SERVICE) as? UiModeManager
		return uiModeManager?.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION
	}
}
