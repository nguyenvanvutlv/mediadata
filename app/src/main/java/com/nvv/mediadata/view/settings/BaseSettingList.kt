package com.nvv.mediadata.view.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Subtitles
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.nvv.mediadata.R
import com.nvv.mediadata.view.core.ItemNavigation


@Composable
fun BaseSettingList(
	navController: NavController
) {
	val subtitleDisplay = stringResource(R.string.subtitle_display)
	val aboutDisplay = stringResource(R.string.about)
	Surface(
		modifier = Modifier.fillMaxSize()
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
				item {
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
				item {
					ItemNavigation(
						leading = {
							Icon(
								imageVector = Icons.Rounded.Palette,
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
								text = "Theme",
								style = MaterialTheme.typography.titleMedium
							)
						},
						supportingContent = {
							Text(
								text = "Light / Dark / System",
								style = MaterialTheme.typography.bodySmall,
								color = MaterialTheme.colorScheme.primary
							)
						},
						onClick = {
							navController.navigate("settings/theme")
						}
					)
				}
				item {
					ItemNavigation(
						leading = {
							Icon(
								imageVector = Icons.Rounded.Info,
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
								text = aboutDisplay,
								style = MaterialTheme.typography.titleMedium
							)
						},
						supportingContent = {
							Text(
								text = "Version, License & Open Source",
								style = MaterialTheme.typography.bodySmall,
								color = MaterialTheme.colorScheme.primary
							)
						},
						onClick = {
							navController.navigate("settings/about")
						}
					)
				}
			}
		}
	}
}