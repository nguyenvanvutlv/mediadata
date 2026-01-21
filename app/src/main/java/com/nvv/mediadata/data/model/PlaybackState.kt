package com.nvv.mediadata.data.model

import android.graphics.Color
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout

@UnstableApi
enum class VideoScaleMode(val displayName: String, val scaleType: Int) {
	BEST_FIT("Best Fit", AspectRatioFrameLayout.RESIZE_MODE_FIT),
	FIT_SCREEN("Fit Screen", AspectRatioFrameLayout.RESIZE_MODE_FILL),
	FILL("Fill", AspectRatioFrameLayout.RESIZE_MODE_ZOOM),
	RATIO_16_9("16:9", AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH),
	RATIO_4_3("4:3", AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT),
	ORIGINAL("Original", AspectRatioFrameLayout.RESIZE_MODE_FIT);

	companion object {
		fun fromString(data: String): VideoScaleMode {
			VideoScaleMode.entries.forEach { v ->
				if (v.displayName == data) return v
			}
			return FILL
		}
	}
}

@OptIn(UnstableApi::class)
data class PlaybackState(
	val startWith: Long = 0,
	val buffer: Int = 0,
	val isError: Boolean = false,
	val messageError: String = "",
	val isBuffering: Boolean = true,
	val isPlaying: Boolean = false,
	val position: Long = 0,
	val duration: Long = 0,
	val mimeType: String? = null,

	val subtitles: List<TrackModel> = emptyList(),
	val audios: List<TrackModel> = emptyList(),
	val sizeSubtitle: Float = 20f,
	val positionSubtitle: Float = 0.1f,
	val scaleMode: VideoScaleMode = VideoScaleMode.FILL,
	val subtitleTextColor: Int = Color.WHITE,
	val opacity: Int = 30,
	val speed: Float = 1f
)