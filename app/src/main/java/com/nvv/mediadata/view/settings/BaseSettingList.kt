package com.nvv.mediadata.view.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.rounded.ArrowForwardIos
import androidx.compose.material.icons.rounded.Cast
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Subtitles
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.nvv.mediadata.R
import com.nvv.mediadata.data.provide.rememberContext
import com.nvv.mediadata.data.viewmodel.Settings
import com.nvv.mediadata.view.core.ItemNavigation
import java.util.Locale

@Composable
fun BaseSettingList(
	navController: NavController
){
	val context = rememberContext()
	val subtitleDisplay = stringResource(R.string.subtitle_display)
	val languageDisplay = stringResource(R.string.language_display)
	val currentLanguageCode = Settings.getLanguages(context)
	val currentLanguageName = remember(currentLanguageCode) {
		val locale = Locale.forLanguageTag(currentLanguageCode)
		locale.getDisplayName(locale).replaceFirstChar { it.uppercase() }
	}
	Surface(
		modifier = Modifier.fillMaxSize()
	) {
		Box(
			Modifier.fillMaxSize()
		){
			LazyColumn(
				modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
				horizontalAlignment = Alignment.CenterHorizontally
			) {
				item{
					ItemNavigation(
						leading = {
							Icon(
								imageVector = Icons.Rounded.Language,
								contentDescription = null
							)
						},
						trailing = {
							Icon(
								imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
								contentDescription = null
							)
						},
						headline = {
							Text(
								text = languageDisplay,
								style = MaterialTheme.typography.titleMedium
							)
						},
						supportingContent = {
							Text(
								text = currentLanguageName,
								style = MaterialTheme.typography.bodySmall,
								color = MaterialTheme.colorScheme.primary
							)
						},
						onClick = {
							navController.navigate("settings/language")
						}
					)
				}
				item{
					ItemNavigation(
						leading = {
							Icon(
								imageVector = Icons.Rounded.Subtitles,
								contentDescription = null
							)
						},
						trailing = {
							Icon(
								imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
								contentDescription = null
							)
						},
						headline = {
							Text(
								text = subtitleDisplay,
								style = MaterialTheme.typography.titleMedium
							)
						},
						onClick = {
							navController.navigate("settings/subtitle")
						}
					)
				}
			}
		}
	}
}