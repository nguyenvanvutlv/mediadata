package com.nvv.mediadata.view.settings


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingLanguageInSubtitle(
	navController: NavController
) {
	val context = rememberContext()
	var currentLanguage by remember { mutableStateOf(Settings.getLanguages(context)) }
	val languageList = remember {
		Locale.getAvailableLocales()
			.filter { it.language.isNotEmpty() && it.displayLanguage.isNotEmpty() }
			.distinctBy { it.language }
			.map { locale ->
				val name = locale.getDisplayName(locale).replaceFirstChar { it.uppercase() }
				name to locale.language
			}
			.sortedBy { it.first }
	}
	Scaffold(
		topBar = {
			TopAppBar(
				title = {
					Text(stringResource(R.string.language_display))
				},
				navigationIcon = {
					IconButton({
						navController.popBackStack()
					}) {
						Icon(
							imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
							contentDescription = null
						)
					}
				}
			)
		},
		modifier = Modifier
			.windowInsetsPadding(
				WindowInsets.displayCutout.union(WindowInsets.statusBars)
			),
		bottomBar = {
			AnimatedVisibility(true, Modifier) { }
		}
	) {
		Surface(
			Modifier
				.fillMaxSize()
				.padding(it)
		) {
			Box(
				Modifier.fillMaxSize()
			) {
				LazyColumn(
					modifier = Modifier
						.fillMaxSize()
						.padding(horizontal = 16.dp),
					horizontalAlignment = Alignment.CenterHorizontally
				) {
					items(languageList, key = { l -> l.second }) { (name, code) ->
						val isSelected = currentLanguage == code
						ItemNavigation(
							leading = {
								Icon(
									imageVector = Icons.Rounded.Language,
									contentDescription = null,
									tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
								)
							},
							trailing = {
								if (isSelected) {
									Icon(
										imageVector = Icons.Rounded.Check,
										contentDescription = null,
										tint = MaterialTheme.colorScheme.primary
									)
								}
							},
							headline = {
								Text(
									text = name,
									style = MaterialTheme.typography.titleMedium,
									color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
								)
							},
							onClick = {
								Settings.setLanguages(context, code)
								currentLanguage = code
								navController.popBackStack()
							}
						)
					}
				}
			}
		}
	}
}