package com.nvv.mediadata.data.connecttv

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.net.InetAddress
import java.util.concurrent.ConcurrentHashMap


class TvDiscoveryService(
	context: Context,
	private val coroutineScope: CoroutineScope,
) {

	private val nsdManager: NsdManager =
		context.getSystemService(Context.NSD_SERVICE) as NsdManager

	private val discoveredDevices: MutableMap<String, TvDevice> = ConcurrentHashMap()

	private val _devices = MutableStateFlow<List<TvDevice>>(emptyList())
	val devices: StateFlow<List<TvDevice>> = _devices

	@Volatile
	private var discoveryListener: NsdManager.DiscoveryListener? = null

	private var refreshJob: Job? = null


	@Synchronized
	fun startDiscovery() {
		if (discoveryListener != null) {
			Timber.d(
				"TvDiscoveryService: discovery already running",
			)
			return
		}

		val listener = object : NsdManager.DiscoveryListener {
			override fun onDiscoveryStarted(regType: String) {
				Timber.i(
					"TvDiscoveryService: discovery started for type=%s",
					regType,
				)
			}

			override fun onServiceFound(serviceInfo: NsdServiceInfo) {
				val type = serviceInfo.serviceType.orEmpty()
				val expectedTypeFragment = "_mediadata._tcp"
				if (type.contains(expectedTypeFragment)) {
					Timber.d(
						"TvDiscoveryService: service found name=%s type=%s",
						serviceInfo.serviceName,
						type,
					)
					resolveService(serviceInfo)
				} else {
					Timber.d(
						"TvDiscoveryService: ignoring unrelated service name=%s type=%s",
						serviceInfo.serviceName,
						type,
					)
				}
			}

			override fun onServiceLost(serviceInfo: NsdServiceInfo) {
				Timber.d(
					"TvDiscoveryService: service lost name=%s type=%s",
					serviceInfo.serviceName,
					serviceInfo.serviceType,
				)
				removeDevice(serviceInfo.serviceName)
			}

			override fun onDiscoveryStopped(serviceType: String) {
				Timber.i(
					"TvDiscoveryService: discovery stopped type=%s",
					serviceType,
				)
			}

			override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
				Timber.w(
					"TvDiscoveryService: start discovery failed type=%s error=%d",
					serviceType,
					errorCode,
				)
				stopDiscoveryInternal()
			}

			override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
				Timber.w(
					"TvDiscoveryService: stop discovery failed type=%s error=%d",
					serviceType,
					errorCode,
				)
				stopDiscoveryInternal()
			}
		}

		discoveryListener = listener
		discoveredDevices.clear()
		_devices.value = emptyList()

		Timber.d(
			"TvDiscoveryService: starting discovery for type=%s",
			TvNsdPublisher.DEFAULT_SERVICE_TYPE,
		)
		nsdManager.discoverServices(
			TvNsdPublisher.DEFAULT_SERVICE_TYPE,
			NsdManager.PROTOCOL_DNS_SD,
			listener,
		)

		refreshJob?.cancel()
		refreshJob = coroutineScope.launch(Dispatchers.Default) {
			// no-op refresh loop for now
		}
	}


	@Synchronized
	fun stopDiscovery() {
		stopDiscoveryInternal()
		refreshJob?.cancel()
		refreshJob = null
	}

	private fun stopDiscoveryInternal() {
		val listener = discoveryListener ?: return
		discoveryListener = null
		runCatching {
			Timber.d(
				"TvDiscoveryService: stopping discovery",
			)
			nsdManager.stopServiceDiscovery(listener)
		}.onFailure {
			Timber.w(it, "TvDiscoveryService: failed to stop discovery")
		}
	}

	private fun resolveService(serviceInfo: NsdServiceInfo) {
		nsdManager.resolveService(
			serviceInfo,
			object : NsdManager.ResolveListener {
				override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
					Timber.w(
						"TvDiscoveryService: resolve failed name=%s type=%s error=%d",
						serviceInfo.serviceName,
						serviceInfo.serviceType,
						errorCode,
					)
				}

				override fun onServiceResolved(resolvedInfo: NsdServiceInfo) {
					val host: InetAddress = resolvedInfo.host ?: return
					val port: Int = resolvedInfo.port
					val name: String = resolvedInfo.serviceName

					Timber.i(
						"TvDiscoveryService: resolved name=%s host=%s port=%d",
						name,
						host.hostAddress,
						port,
					)

					addOrUpdateDevice(
						TvDevice(
							name = name,
							host = host,
							port = port,
						),
					)
				}
			},
		)
	}

	private fun addOrUpdateDevice(device: TvDevice) {
		discoveredDevices[device.name] = device
		_devices.value = discoveredDevices.values.sortedBy { it.name }
	}

	private fun removeDevice(name: String) {
		discoveredDevices.remove(name)
		_devices.value = discoveredDevices.values.sortedBy { it.name }
	}
}

