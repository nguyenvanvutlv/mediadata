package com.nvv.mediadata.data.connecttv

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


object TvPlayRequestStore {

	private val _latestUrl = MutableStateFlow<String?>(null)


	val latestUrl: StateFlow<String?> = _latestUrl


	fun onRemotePlayRequested(url: String) {
		_latestUrl.value = url
	}
}

