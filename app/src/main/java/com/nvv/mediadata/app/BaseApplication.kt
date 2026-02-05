package com.nvv.mediadata.app

import android.app.Application
import android.app.UiModeManager
import android.content.res.Configuration
import androidx.annotation.OptIn
import com.nvv.mediadata.BuildConfig
import com.nvv.mediadata.data.connecttv.TvConnectManager
import com.nvv.mediadata.data.connecttv.TvPlayRequestCallback
import com.nvv.mediadata.data.connecttv.TvPlayRequestStore
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.UnstableApi
import timber.log.Timber

@OptIn(UnstableApi::class)
@HiltAndroidApp
class BaseApplication : Application() {
	private var tvConnectManager: TvConnectManager? = null

	override fun onCreate() {
		super.onCreate()
		if (BuildConfig.DEBUG) {
			Timber.plant(Timber.DebugTree())
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
