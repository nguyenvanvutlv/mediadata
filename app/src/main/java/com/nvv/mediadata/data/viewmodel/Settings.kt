package com.nvv.mediadata.data.viewmodel

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.OptIn
import androidx.core.content.edit
import androidx.media3.common.util.UnstableApi
import com.nvv.mediadata.data.model.VideoScaleMode
import android.graphics.Color as AndroidColor

@OptIn(UnstableApi::class)
class Settings {
	companion object {
		private const val APPLICATION_ID = "com.nvv.mediadata"
		private const val FOLDER = "folder"
		private const val LANGUAGES = "languages"
		private const val CAST_INDEX = "cast_index"

		//// player
		private const val SUBTITLE_COLOR = "subtitle_color"
		private const val SUBTITLE_OUTLINE_COLOR = "subtitle_outline_color"
		private const val SUBTITLE_BACKGROUND_COLOR = "subtitle_background_color"
		private const val BACKGROUND_COLOR_OPACITY = "background_color_opacity"
		private const val SUBTITLE_SIZE = "subtitle_size"
		private const val SUBTITLE_POSITION = "subtitle_position"
		private const val SUBTITLE_FONT = "subtitle_font"
		private const val SECONDARY_LANGUAGE = "secondary_language"
		private const val SCALE_MODE = "scale_mode"
		private const val THEME_MODE = "theme_mode"

		enum class ThemeMode {
			SYSTEM, LIGHT, DARK
		}


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
			getSharedPreferences(context).edit { putString(LANGUAGES, languages) }
		}

		fun getSubtitleSize(context: Context): Float {
			return getSharedPreferences(context).getFloat(SUBTITLE_SIZE, 20f)
		}

		fun setSubtitleSize(context: Context, size: Float) {
			getSharedPreferences(context).edit { putFloat(SUBTITLE_SIZE, size) }
		}

		fun getSubtitlePosition(context: Context): Float {
			return getSharedPreferences(context).getFloat(SUBTITLE_POSITION, 0.1f)
		}

		fun setSubtitlePosition(context: Context, position: Float) {
			getSharedPreferences(context).edit { putFloat(SUBTITLE_POSITION, position) }
		}

		fun getSubtitleFont(context: Context): String {
			return getSharedPreferences(context).getString(SUBTITLE_FONT, "DEFAULT") ?: "DEFAULT"
		}

		fun setSubtitleFont(context: Context, fontFamilyType: String) {
			getSharedPreferences(context).edit { putString(SUBTITLE_FONT, fontFamilyType) }
		}

		fun getScaleMode(context: Context): VideoScaleMode {
			return VideoScaleMode.fromString(
				getSharedPreferences(context).getString(
					SCALE_MODE, VideoScaleMode.FILL.displayName
				) ?: VideoScaleMode.FILL.displayName
			)
		}

		fun setScaleMode(context: Context, mode: VideoScaleMode) {
			getSharedPreferences(context).edit { putString(SCALE_MODE, mode.displayName) }
		}

		fun getColor(context: Context): AndroidColor {
			return AndroidColor.valueOf(
				getSharedPreferences(context).getInt(SUBTITLE_COLOR, AndroidColor.WHITE)
			)
		}

		fun setColor(context: Context, color: AndroidColor) {
			getSharedPreferences(context).edit { putInt(SUBTITLE_COLOR, color.toArgb()) }
		}

		fun getSubtitleOutlineColor(context: Context): Int {
			return getSharedPreferences(context).getInt(SUBTITLE_OUTLINE_COLOR, AndroidColor.BLACK)
		}

		fun setSubtitleOutlineColor(context: Context, color: Int) {
			getSharedPreferences(context).edit { putInt(SUBTITLE_OUTLINE_COLOR, color) }
		}

		fun getSubtitleBackgroundColor(context: Context): Int {
			return getSharedPreferences(context).getInt(SUBTITLE_BACKGROUND_COLOR, AndroidColor.TRANSPARENT)
		}

		fun setSubtitleBackgroundColor(context: Context, color: Int) {
			getSharedPreferences(context).edit { putInt(SUBTITLE_BACKGROUND_COLOR, color) }
		}

		fun getBackgroundColorOpacity(context: Context): Int {
			return getSharedPreferences(context).getInt(BACKGROUND_COLOR_OPACITY, 30)
		}

		fun setBackgroundColorOpacity(context: Context, opacity: Int) {
			getSharedPreferences(context).edit { putInt(BACKGROUND_COLOR_OPACITY, opacity) }
		}

		fun getPath(context: Context): String {
			return getSharedPreferences(context).getString(FOLDER, "") ?: ""
		}

		fun setPath(context: Context, path: String) {
			getSharedPreferences(context).edit { putString(FOLDER, path) }
		}

		fun getThemeMode(context: Context): ThemeMode {
			val modeName = getSharedPreferences(context).getString(THEME_MODE, ThemeMode.SYSTEM.name)
			return try {
				ThemeMode.valueOf(modeName ?: ThemeMode.SYSTEM.name)
			} catch (e: Exception) {
				ThemeMode.SYSTEM
			}
		}

		fun setThemeMode(context: Context, mode: ThemeMode) {
			getSharedPreferences(context).edit { putString(THEME_MODE, mode.name) }
		}
	}
}