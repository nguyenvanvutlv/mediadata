package com.nvv.mediadata.data.viewmodel

import android.content.Context
import android.content.SharedPreferences

class Settings {
	companion object {
		private const val APPLICATION_ID = "com.nvv.mediadata"

		private const val LANGUAGES = "languages"

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
	}
}