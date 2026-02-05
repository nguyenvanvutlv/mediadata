package com.nvv.mediadata.data.connecttv

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import timber.log.Timber


class TvNsdPublisher(
	private val context: Context,
	private val port: Int,
	private val serviceName: String = DEFAULT_SERVICE_NAME,
	private val serviceType: String = DEFAULT_SERVICE_TYPE,
) {

	companion object {
		const val DEFAULT_SERVICE_NAME: String = "MediaData-TV"
		const val DEFAULT_SERVICE_TYPE: String = "_mediadata._tcp."
	}

	private val nsdManager: NsdManager =
		context.getSystemService(Context.NSD_SERVICE) as NsdManager

	@Volatile
	private var registrationListener: NsdManager.RegistrationListener? = null


	@Synchronized
	fun register() {
		if (registrationListener != null) {
			Timber.d(
				"TvNsdPublisher: service already registered"
			)
			return
		}

		val serviceInfo = NsdServiceInfo().apply {
			serviceName = this@TvNsdPublisher.serviceName
			serviceType = this@TvNsdPublisher.serviceType
			port = this@TvNsdPublisher.port
		}

		val listener = object : NsdManager.RegistrationListener {
			override fun onServiceRegistered(registeredServiceInfo: NsdServiceInfo) {
				Timber.i(
					"TvNsdPublisher: service registered name=%s type=%s port=%d",
					registeredServiceInfo.serviceName,
					registeredServiceInfo.serviceType,
					registeredServiceInfo.port,
				)
			}

			override fun onRegistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
				Timber.w(
					"TvNsdPublisher: registration failed name=%s type=%s error=%d",
					serviceInfo.serviceName,
					serviceInfo.serviceType,
					errorCode,
				)
			}

			override fun onServiceUnregistered(serviceInfo: NsdServiceInfo) {
				Timber.i(
					"TvNsdPublisher: service unregistered name=%s type=%s",
					serviceInfo.serviceName,
					serviceInfo.serviceType,
				)
			}

			override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
				Timber.w(
					"TvNsdPublisher: unregistration failed name=%s type=%s error=%d",
					serviceInfo.serviceName,
					serviceInfo.serviceType,
					errorCode,
				)
			}
		}

		registrationListener = listener
		Timber.d(
			"TvNsdPublisher: registering NSD service name=%s type=%s port=%d",
			serviceName,
			serviceType,
			port,
		)
		nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, listener)
	}


	@Synchronized
	fun unregister() {
		val listener = registrationListener ?: return
		registrationListener = null
		runCatching {
			Timber.d(
				"TvNsdPublisher: unregistering NSD service",
			)
			nsdManager.unregisterService(listener)
		}.onFailure {
			Timber.w(it, "TvNsdPublisher: failed to unregister service")
		}
	}
}

