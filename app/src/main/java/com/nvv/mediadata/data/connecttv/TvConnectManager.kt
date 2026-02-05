package com.nvv.mediadata.data.connecttv

import android.app.Application
import android.app.UiModeManager
import android.content.Context
import android.content.res.Configuration
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner


class TvConnectManager(
	private val application: Application,
	private val port: Int = TvHttpPlayServer.DEFAULT_PORT,
	private val playRequestCallback: TvPlayRequestCallback,
) : DefaultLifecycleObserver {

	private val isTv: Boolean
		get() {
			val uiModeManager = application.getSystemService(Context.UI_MODE_SERVICE) as? UiModeManager
			return uiModeManager?.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION
		}

	@Volatile
	private var httpServer: TvHttpPlayServer? = null

	@Volatile
	private var nsdPublisher: TvNsdPublisher? = null


	fun attachToProcessLifecycle() {
		ProcessLifecycleOwner.get().lifecycle.addObserver(this)
	}

	override fun onStart(owner: LifecycleOwner) {
		super.onStart(owner)
		if (!isTv) {
			return
		}
		startServerAndNsdIfNeeded()
	}

	override fun onStop(owner: LifecycleOwner) {
		super.onStop(owner)
		if (!isTv) {
			return
		}
		stopServerAndNsd()
	}

	private fun startServerAndNsdIfNeeded() {
		if (httpServer != null || nsdPublisher != null) {
			return
		}
		val server = TvHttpPlayServer(
			port = port,
			callback = playRequestCallback,
		)
		httpServer = server

		try {
			server.start(SOCKET_READ_TIMEOUT, false)
		} catch (t: Throwable) {
			httpServer = null
			return
		}

		val publisher = TvNsdPublisher(
			context = application,
			port = port,
		)
		nsdPublisher = publisher
		publisher.register()
	}

	private fun stopServerAndNsd() {
		nsdPublisher?.unregister()
		nsdPublisher = null

		httpServer?.let { server ->
			runCatching {
				server.stop()
			}.onFailure {
			}
		}
		httpServer = null
	}

	companion object {
		private const val SOCKET_READ_TIMEOUT: Int = 5_000
	}
}

