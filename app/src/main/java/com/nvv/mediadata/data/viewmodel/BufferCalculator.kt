package com.nvv.mediadata.data.viewmodel

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import timber.log.Timber

const val DEFAULT_BUFFER_FOR_PLAYBACK = 2_500
const val DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER = 3_000
const val DEFAULT_MAX_BUFFER = 30_000
const val DEFAULT_MIN_BUFFER = 6_000

object BufferCalculator {
	fun calculateBufferConfig(context: Context): BufferConfig {
		val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
		val memoryInfo = ActivityManager.MemoryInfo()
		activityManager?.getMemoryInfo(memoryInfo)
		val availableRamMB = memoryInfo.availMem / (1024 * 1024)
		val totalRamMB = memoryInfo.totalMem / (1024 * 1024)
		val isLowMemoryDevice = memoryInfo.lowMemory
		val androidVersionFactor = when {
			Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> 1.2f
			else -> 0.9f
		}
		val ramMultiplier = when {
			isLowMemoryDevice || availableRamMB < 1_000 -> 0.4f
			availableRamMB < 2_000 -> 0.6f
			availableRamMB < 3_000 -> 0.8f
			availableRamMB < 4_000 -> 1.0f
			availableRamMB < 6_000 -> 1.2f
			else -> 1.5f
		}
		val finalMultiplier = (ramMultiplier * androidVersionFactor).coerceIn(0.3f, 1.8f)
		val minBufferLimit = if (isLowMemoryDevice || availableRamMB < 2_000) {
			Pair(2_000, 6_000)
		} else {
			Pair(3_000, 10_000)
		}
		val maxBufferLimit = if (isLowMemoryDevice || availableRamMB < 2_000) {
			Pair(12_000, 25_000)
		} else {
			Pair(15_000, 60_000)
		}
		val minBuffer = (DEFAULT_MIN_BUFFER * finalMultiplier).toInt().coerceIn(minBufferLimit.first, minBufferLimit.second)
		val maxBuffer = (DEFAULT_MAX_BUFFER * finalMultiplier).toInt().coerceIn(maxBufferLimit.first, maxBufferLimit.second)
		val bufferForPlayback = (DEFAULT_BUFFER_FOR_PLAYBACK * finalMultiplier).toInt().coerceIn(1_000, 5_000)
		val bufferForPlaybackAfterRebuffer = (DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER * finalMultiplier).toInt().coerceIn(1_500, 6_000)
		val safeMinBuffer = minBuffer.coerceAtMost(maxBuffer - 2_000)
		Timber.tag("BufferCalculator").d("Device RAM: ${totalRamMB}MB total, ${availableRamMB}MB available")
		Timber.tag("BufferCalculator").d("Low memory device: $isLowMemoryDevice")
		Timber.tag("BufferCalculator").d("Android version: ${Build.VERSION.SDK_INT}, factor: $androidVersionFactor")
		Timber.tag("BufferCalculator").d("RAM multiplier: $ramMultiplier, final multiplier: $finalMultiplier")
		Timber.tag("BufferCalculator").d("Calculated buffers - min: ${safeMinBuffer}ms, max: ${maxBuffer}ms, playback: ${bufferForPlayback}ms, afterRebuffer: ${bufferForPlaybackAfterRebuffer}ms")
		return BufferConfig(
			minBuffer = safeMinBuffer,
			maxBuffer = maxBuffer,
			bufferForPlayback = bufferForPlayback,
			bufferForPlaybackAfterRebuffer = bufferForPlaybackAfterRebuffer
		)
	}

	data class BufferConfig(
		val minBuffer: Int,
		val maxBuffer: Int,
		val bufferForPlayback: Int,
		val bufferForPlaybackAfterRebuffer: Int
	)
}
