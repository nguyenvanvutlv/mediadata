package com.nvv.mediadata.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.nvv.mediadata.data.viewmodel.Settings

private val DarkColorScheme = darkColorScheme(
	primary = Purple80,
	secondary = PurpleGrey80,
	tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
	primary = Purple40,
	secondary = PurpleGrey40,
	tertiary = Pink40
)

@Composable
fun MediadataTheme(
	darkTheme: Boolean = isSystemInDarkTheme(),
	dynamicColor: Boolean = true,
	themeMode: Settings.Companion.ThemeMode = Settings.Companion.ThemeMode.SYSTEM,
	content: @Composable () -> Unit
) {
	val colorScheme = when {
		dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
			val context = LocalContext.current
			val isDark = when (themeMode) {
				Settings.Companion.ThemeMode.SYSTEM -> darkTheme
				Settings.Companion.ThemeMode.DARK -> true
				Settings.Companion.ThemeMode.LIGHT -> false
			}
			if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
		}

		themeMode == Settings.Companion.ThemeMode.DARK -> DarkColorScheme
		themeMode == Settings.Companion.ThemeMode.LIGHT -> LightColorScheme
		darkTheme -> DarkColorScheme
		else -> LightColorScheme
	}

	MaterialTheme(
		colorScheme = colorScheme,
		typography = Typography,
		content = content
	)
}