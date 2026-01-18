package com.nvv.mediadata.data.model

import androidx.media3.ui.AspectRatioFrameLayout

enum class VideoScaleMode(val displayName: String, val scaleType: Int) {
	BEST_FIT("Best Fit", AspectRatioFrameLayout.RESIZE_MODE_FIT),
	FIT_SCREEN("Fit Screen", AspectRatioFrameLayout.RESIZE_MODE_FILL),
	FILL("Fill", AspectRatioFrameLayout.RESIZE_MODE_ZOOM),
	RATIO_16_9("16:9", AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH),
	RATIO_4_3("4:3", AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT),
	ORIGINAL("Original", AspectRatioFrameLayout.RESIZE_MODE_FIT)
}

data class PlaybackState(
	var startWith: Long = 0,
	var buffer: Int = 0,
	var isError: Boolean = false,
	var messageError: String = "",
	var isBuffering: Boolean = true,
	var isPlaying: Boolean = false,
	var position: Long = 0,
	var duration: Long = 0,
	var mimeType: String? = null,


	val sizeSubtitle: Float = 20f,
	val positionSubtitle: Float = 0.1f,
	var scaleMode: VideoScaleMode = VideoScaleMode.FILL,
)