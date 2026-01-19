package com.nvv.mediadata.view.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.nvv.mediadata.R
import com.nvv.mediadata.data.provide.rememberContext
import com.nvv.mediadata.data.viewmodel.Settings
import com.nvv.mediadata.view.core.ItemNavigation
import com.nvv.mediadata.view.core.toAndroidColor
import com.nvv.mediadata.view.core.toComposeColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingSubtitle(
	navController: NavController
){
	val context = rememberContext()
	val subtitleDisplay = stringResource(R.string.subtitle_display)
	val leadingSubtitleColor = stringResource(R.string.leading_text_color)
	val leadingSubtitleBackgroundColorOpacity =
		stringResource(R.string.leading_background_color_opacity)
	val subtitleTextColors = listOf(
		"White" to Color.White,
		"Yellow" to Color.Yellow,
		"Red" to Color.Red,
		"Black" to Color.Black,
		"Blue" to Color.Blue,
		"Green" to Color.Green,
		"Magenta" to Color.Magenta,
	)
	var isOpenSubtitleColor by remember { mutableStateOf(false) }
	var isOpenSubtitleBackgroundColorOpacity by remember { mutableStateOf(false) }
	var currentSubtitleTextColor by remember { mutableStateOf(
		Settings.getColor(context)) }
	var currentBackgroundColorOpacity by remember { mutableStateOf(
		Settings.getBackgroundColorOpacity(context)) }
	Scaffold(
		topBar = {
			CenterAlignedTopAppBar(
				title = {
					Text(subtitleDisplay,
						style = MaterialTheme.typography.titleLarge
					)
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
			Modifier.fillMaxSize().padding(it)
		) {
			Box(
				Modifier.fillMaxSize()
			){
				LazyColumn(
					modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
					horizontalAlignment = Alignment.CenterHorizontally
				) {
					// SUBTITLE TEXT COLOR
					item{
						ItemNavigation(
							leading = {
								Text(leadingSubtitleColor,
									style = MaterialTheme.typography.titleMedium
								)
							},
							trailing = {
								subtitleTextColors.firstOrNull { item ->
									item.second == currentSubtitleTextColor.toComposeColor()
								}?.let { c ->
									Text(c.first,
										style = MaterialTheme.typography.titleMedium
									)
								}
							},
							onClick = {
								isOpenSubtitleColor = true
							}
						)
					}
					// SUBTITLE BACKGROUND COLOR
					item{
						ItemNavigation(
							leading = {
								Text(leadingSubtitleBackgroundColorOpacity,
									style = MaterialTheme.typography.titleMedium
								)
							},
							trailing = {
								Text(
									"$currentBackgroundColorOpacity%",
									style = MaterialTheme.typography.titleMedium
								)
							},
							onClick = {
								isOpenSubtitleBackgroundColorOpacity = true
							}
						)
					}
				}

				DropdownMenu(
					expanded = isOpenSubtitleColor,
					onDismissRequest = { isOpenSubtitleColor = false },
					modifier = Modifier.align(Alignment.BottomCenter)
						.padding(horizontal = 20.dp, vertical = 10.dp)
				) {
					subtitleTextColors.forEach { (name, color) ->
						DropdownMenuItem(
							text = {
								Text(name,
									style = MaterialTheme.typography.bodyMedium
								)
							},
							leadingIcon = {
								Box(
									modifier = Modifier
										.size(30.dp)
										.background(
											color = color,
											shape = CircleShape
										)
								)
							},
							onClick = {
								currentSubtitleTextColor = color.toAndroidColor()
								Settings.setColor(context, currentSubtitleTextColor)
								isOpenSubtitleColor = false
							}
						)
					}
				}
				DropdownMenu(
					expanded = isOpenSubtitleBackgroundColorOpacity,
					onDismissRequest = { isOpenSubtitleBackgroundColorOpacity = false },
					modifier = Modifier.fillMaxWidth(0.8f)
				) {
					Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
						Slider(
							value = currentBackgroundColorOpacity.toFloat(),
							onValueChange = { v ->
								currentBackgroundColorOpacity = v.toInt()
								Settings.setBackgroundColorOpacity(context, currentBackgroundColorOpacity)
							},
							valueRange = 0f..100f
						)
					}
				}
			}
		}
	}
}