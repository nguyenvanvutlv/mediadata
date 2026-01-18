package com.nvv.mediadata.data.model

import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.TrackGroup
import java.util.Locale

data class TrackModel(
	var id: String = "0",
	var name: String = "",
	var isSelected: Boolean = false,
	val group: TrackGroup? = null,
	val format: Format? = null,
	val trackType: Int? = null
)

fun exoLabel(format: Format?, trackType: Int?): String {
	val lang = format?.language
		?.takeIf { it.isNotBlank() && it.lowercase() != "und" }
		?.let {
			try {
				val loc = Locale.forLanguageTag(it)
				loc.getDisplayLanguage(Locale.ENGLISH)
			} catch (_: Exception) {
				it
			}
		}
	return when (trackType) {
		C.TRACK_TYPE_VIDEO -> {
			lang ?: "Video Track"
		}
		C.TRACK_TYPE_AUDIO -> {
			lang ?: "Audio Track"
		}
		C.TRACK_TYPE_TEXT -> {
			lang ?: "Subtitle Track"
		}
		else -> {
			lang ?: "Track"
		}
	}
}