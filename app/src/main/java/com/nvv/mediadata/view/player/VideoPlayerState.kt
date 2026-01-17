package com.nvv.mediadata.view.player

import android.annotation.SuppressLint
import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.annotation.IntRange
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.nvv.mediadata.data.provide.rememberContext
import com.nvv.mediadata.view.core.findActivity
import dagger.hilt.android.UnstableApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.debounce

@androidx.annotation.OptIn(UnstableApi::class)
class VideoPlayerState(
	@IntRange(from = 0)
	private val hideSeconds: Int,
	private val window: Window?,
) {
	var isControlsVisible by mutableStateOf(true)
		private set

	fun showControls(isPlaying: Boolean = true) {
		if (isPlaying) {
			updateControlVisibility()
		} else {
			updateControlVisibility(seconds = Int.MAX_VALUE)
		}
	}

	fun hideControls() {
		// hide top bar
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
			window?.insetsController?.apply {
				hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
				systemBarsBehavior =
					WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
			}
		}
		else {
			@Suppress("DEPRECATION")
			window?.decorView?.systemUiVisibility =
				(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
						or View.SYSTEM_UI_FLAG_FULLSCREEN
						or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
		}
		isControlsVisible = false
	}

	private fun updateControlVisibility(seconds: Int = hideSeconds) {
		isControlsVisible = true
		channel.trySend(seconds)
	}

	private val channel = Channel<Int>(CONFLATED)

	@OptIn(FlowPreview::class)
	suspend fun observe() {
		channel.consumeAsFlow()
			.debounce { it.toLong() * 1000 }
			.collect {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
					window?.insetsController?.apply {
						hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
						systemBarsBehavior =
							WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
					}
				}
				else {
					@Suppress("DEPRECATION")
					window?.decorView?.systemUiVisibility =
						(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
								or View.SYSTEM_UI_FLAG_FULLSCREEN
								or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
				}
				isControlsVisible = false
			}
	}
}

@SuppressLint("SuspiciousIndentation")
@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun rememberVideoPlayerState(
	@IntRange(from = 0) hideSeconds: Int = 2
): VideoPlayerState {
	val context = rememberContext()
	val activity = context.findActivity()
	val window: Window? = activity?.window
	return remember {
		VideoPlayerState(
			hideSeconds = hideSeconds,
			window = window
		)
	}.also { LaunchedEffect(it) { it.observe() } }
}