package com.nvv.mediadata.data.viewmodel

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color as AndroidColor
import com.nvv.mediadata.data.model.VideoScaleMode

class Settings {
	companion object {
		private const val APPLICATION_ID = "com.nvv.mediadata"

		private const val LANGUAGES = "languages"
		private const val CAST_INDEX = "cast_index"

		//// player
		private const val SUBTITLE_COLOR = "subtitle_color"
		private const val BACKGROUND_COLOR_OPACITY = "background_color_opacity"
		private const val SUBTITLE_SIZE = "subtitle_size"
		private const val SUBTITLE_POSITION = "subtitle_position"
		private const val SCALE_MODE = "scale_mode"


		private fun getSharedPreferences(context: Context): SharedPreferences {
			return context.getSharedPreferences(
				APPLICATION_ID,
				Context.MODE_PRIVATE
			)
		}

		fun getLanguages(context: Context): String {
			return getSharedPreferences(context).getString(LANGUAGES, "en") ?: "en"
		}

		fun setLanguages(context: Context, languages: String) {
			getSharedPreferences(context).edit().putString(LANGUAGES, languages).apply()
		}

		fun getSubtitleSize(context: Context) : Float {
			return getSharedPreferences(context).getFloat(SUBTITLE_SIZE, 20f)
		}

		fun setSubtitleSize(context: Context, size: Float) {
			getSharedPreferences(context).edit().putFloat(SUBTITLE_SIZE, size).apply()
		}

		fun getSubtitlePosition(context: Context) : Float {
			return getSharedPreferences(context).getFloat(SUBTITLE_POSITION, 0.1f)
		}

		fun setSubtitlePosition(context: Context, position: Float) {
			getSharedPreferences(context).edit().putFloat(SUBTITLE_POSITION, position).apply()
		}

		fun getScaleMode(context: Context) : VideoScaleMode {
			return VideoScaleMode.fromString(
				getSharedPreferences(context).getString(
					SCALE_MODE, VideoScaleMode.FILL.displayName) ?: VideoScaleMode.FILL.displayName
			)
		}

		fun setScaleMode(context: Context, mode: VideoScaleMode) {
			getSharedPreferences(context).edit().putString(SCALE_MODE, mode.displayName).apply()
		}

		fun getColor(context: Context) : AndroidColor {
			return AndroidColor.valueOf(
				getSharedPreferences(context).getInt(SUBTITLE_COLOR, AndroidColor.WHITE)
			)
		}

		fun setColor(context: Context, color: AndroidColor) {
			getSharedPreferences(context).edit().putInt(SUBTITLE_COLOR, color.toArgb()).apply()
		}

		fun getBackgroundColorOpacity(context: Context) : Int {
			return getSharedPreferences(context).getInt(BACKGROUND_COLOR_OPACITY, 30)
		}

		fun setBackgroundColorOpacity(context: Context, opacity: Int) {
			getSharedPreferences(context).edit().putInt(BACKGROUND_COLOR_OPACITY, opacity).apply()
		}
	}
}