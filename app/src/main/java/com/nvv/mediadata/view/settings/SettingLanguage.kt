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
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.nvv.mediadata.view.core.ItemNavigation
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingLanguage(
	navController: NavController
){
	val locales = Locale.getAvailableLocales()
	val languageList = locales.map { locale ->
		locale.getDisplayName(locale).replaceFirstChar { it.uppercase() }
	}.distinct().sorted()
	Scaffold(
		topBar = {
			TopAppBar(
				title = {
					Text("Language")
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
				},
				modifier = Modifier
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
			Modifier.fillMaxSize().padding(it)
		) {
			Box(
				Modifier.fillMaxSize()
			) {
				LazyColumn(
					modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
					horizontalAlignment = Alignment.CenterHorizontally
				) {
					items(languageList, key = { idx ->
						idx
					}){ item ->
						ItemNavigation(
							leading = {

							},
							trailing = {

							},
							headline = {
								Text(
									text = item,
									style = MaterialTheme.typography.titleMedium
								)
							},
							onClick = {

							}
						)
					}
				}
			}
		}
	}
}