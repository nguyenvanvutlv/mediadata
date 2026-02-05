package com.nvv.mediadata.data.connecttv

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named


@HiltViewModel
class TvDiscoveryViewModel @Inject constructor(
	@ApplicationContext context: Context,
	@Named("BaseOkHttpClient") private val okHttpClient: OkHttpClient,
) : ViewModel() {

	private val discoveryService = TvDiscoveryService(
		context = context,
		coroutineScope = viewModelScope,
	)

	val devices: StateFlow<List<TvDevice>> = discoveryService.devices

	init {
		discoveryService.startDiscovery()
	}


	fun refresh() {
		discoveryService.stopDiscovery()
		discoveryService.startDiscovery()
	}


	fun sendUrl(tvDevice: TvDevice, url: String) {
		viewModelScope.launch {
			val result = sendUrlToTv(
				client = okHttpClient,
				tv = tvDevice,
				url = url,
			)
			result
				.onSuccess {
					Timber.i(
						"TvDiscoveryViewModel: successfully sent URL to TV=%s",
						tvDevice.name,
					)
				}
				.onFailure {
					Timber.w(
						it,
						"TvDiscoveryViewModel: failed to send URL to TV=%s",
						tvDevice.name,
					)
				}
		}
	}

	override fun onCleared() {
		super.onCleared()
		discoveryService.stopDiscovery()
	}
}

