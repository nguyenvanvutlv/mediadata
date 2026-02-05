package com.nvv.mediadata.data.services

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.display.DisplayManager
import android.view.Display

object HdrDisplaySupportChecker {
	@JvmStatic
	fun isDisplayHdrCapable(context: Context): Boolean {
		val display = getDisplayForCapabilities(context) ?: return false
		return isDisplayHdrCapableApi24(display)
	}

	@JvmStatic
	fun getDisplayForCapabilities(context: Context): Display? {
		val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager ?: return null

		@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
		val displays = displayManager.displays
		return if (displays.isNotEmpty()) displays[0] else null
	}

	@SuppressLint("SuspiciousIndentation")
	private fun isDisplayHdrCapableApi24(display: Display): Boolean {
		val hdrCapabilities = display.hdrCapabilities ?: return false
		return hdrCapabilities.desiredMaxLuminance > 0f
	}
}