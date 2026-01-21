package com.nvv.mediadata.view.player

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Audiotrack
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Subtitles
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nvv.mediadata.R
import com.nvv.mediadata.data.model.PlaybackState
import com.nvv.mediadata.data.viewmodel.PlayerViewModel


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SettingPlayer(
	viewModel: PlayerViewModel,
) {
	val navController = rememberNavController()
	val state by viewModel.state.collectAsStateWithLifecycle()
	Box(
		modifier = Modifier
			.fillMaxSize(),
		contentAlignment = Alignment.Center
	) {
		Surface(
			modifier = Modifier
				.width(350.dp)
				.heightIn(min = 200.dp, max = 400.dp)
				.clickable(enabled = false) {}, // Prevent clicks passing through
			shape = RoundedCornerShape(16.dp),
			color = MaterialTheme.colorScheme.surface,
			tonalElevation = 8.dp
		) {
			NavHost(
				navController = navController,
				startDestination = "menu",
				enterTransition = {
					slideInHorizontally(
						initialOffsetX = { it },
						animationSpec = tween(300)
					) + fadeIn(animationSpec = tween(300))
				},
				exitTransition = {
					slideOutHorizontally(
						targetOffsetX = { -it / 3 },
						animationSpec = tween(300)
					) + fadeOut(animationSpec = tween(300))
				},
				popEnterTransition = {
					slideInHorizontally(
						initialOffsetX = { -it / 2 },
						animationSpec = tween(300)
					) + fadeIn(animationSpec = tween(300))
				},
				popExitTransition = {
					slideOutHorizontally(
						targetOffsetX = { it },
						animationSpec = tween(300)
					) + fadeOut(animationSpec = tween(300))
				},
			) {
				composable("menu") {
					MenuScreen(navController)
				}
				composable("audio") {
					AudioTrackScreen(
						tracks = viewModel.getAudioTracks(),
						onTrackSelected = {
							viewModel.selectAudioTrack(it)
							navController.popBackStack()
						},
						onBack = { navController.popBackStack() }
					)
				}
				composable("subtitle") {
					SubtitleTrackScreen(
						tracks = viewModel.getSubtitleTracks(),
						onTrackSelected = {
							viewModel.selectSubtitleTrack(it)
							navController.popBackStack()
						},
						onBack = { navController.popBackStack() }
					)
				}
				composable("settings") {
					SubtitleSettingScreen(
						state = state,
						onUpdatePosition = { p ->
							viewModel.updateSubtitlePosition(p)
						},
						onUpdateSize = { p ->
							viewModel.updateSubtitleSize(p)
						},
						onBack = {
							navController.popBackStack()
						}
					)
				}
			}
		}
	}
}

@Composable
fun MenuScreen(navController: NavController) {
	Column(modifier = Modifier.padding(vertical = 8.dp)) {
		MenuItem(
			icon = Icons.Rounded.Audiotrack,
			text = stringResource(R.string.audio_track_title),
			onClick = { navController.navigate("audio") }
		)
		MenuItem(
			icon = Icons.Rounded.Subtitles,
			text = stringResource(R.string.subtitle_track_title),
			onClick = { navController.navigate("subtitle") }
		)
		MenuItem(
			icon = Icons.Rounded.Build,
			text = stringResource(R.string.subtitle_settings_title),
			onClick = { navController.navigate("settings") }
		)
	}
}

@Composable
fun MenuItem(
	icon: ImageVector,
	text: String,
	secondaryText: String? = null,
	onClick: () -> Unit
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.clickable(onClick = onClick)
			.padding(horizontal = 16.dp, vertical = 12.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		Icon(
			imageVector = icon, contentDescription = null,
			tint = MaterialTheme.colorScheme.onSurface
		)
		Spacer(modifier = Modifier.width(16.dp))
		Text(
			text = text, style = MaterialTheme.typography.bodyLarge,
			color = MaterialTheme.colorScheme.onSurface
		)
		Spacer(modifier = Modifier.weight(1f))
		if (secondaryText != null) {
			Text(
				text = secondaryText, style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
		}
		Icon(
			imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
			contentDescription = null,
			tint = MaterialTheme.colorScheme.onSurfaceVariant
		)
	}
}

@Composable
fun AudioTrackScreen(
	tracks: List<String>,
	onTrackSelected: (Int) -> Unit,
	onBack: () -> Unit
) {
	Column {
		Header(stringResource(R.string.audio_track_title), onBack)
		LazyColumn {
			itemsIndexed(tracks) { index, track ->
				SelectionItem(
					text = track,
					isSelected = false, // Ideally pass selected index
					onClick = { onTrackSelected(index) }
				)
			}
		}
	}
}

@Composable
fun SubtitleTrackScreen(
	tracks: List<String>,
	onTrackSelected: (Int) -> Unit,
	onBack: () -> Unit
) {
	Column {
		Header(stringResource(R.string.subtitle_track_title), onBack)
		LazyColumn {
			itemsIndexed(tracks) { index, track ->
				SelectionItem(
					text = track,
					isSelected = false,
					onClick = { onTrackSelected(index) }
				)
			}
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubtitleSettingScreen(
	state: PlaybackState,
	onUpdatePosition: (Float) -> Unit,
	onUpdateSize: (Float) -> Unit,
	onBack: () -> Unit
) {
	Column {
		Header(stringResource(R.string.subtitle_settings_title), onBack)
		LazyColumn(
			Modifier.fillMaxWidth(0.8f),
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.Center
		) {
			item {
				Text(
					stringResource(
						R.string.subtitle_size_label,
						state.sizeSubtitle.toInt()
					)
				)
				Slider(
					value = state.sizeSubtitle,
					onValueChange = onUpdateSize,
					valueRange = 10f..100f,
					modifier = Modifier.fillMaxWidth()
				)
			}
			item {
				Text(
					stringResource(
						R.string.subtitle_position_label,
						(state.positionSubtitle * 100).toInt()
					)
				)
				Slider(
					value = state.positionSubtitle,
					onValueChange = onUpdatePosition,
					valueRange = 0f..1f,
					modifier = Modifier.fillMaxWidth()
				)
			}
		}
	}
}

@Composable
fun Header(title: String, onBack: () -> Unit) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.clickable(onClick = onBack)
			.padding(16.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		Icon(
			imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
			contentDescription = "Back",
			tint = MaterialTheme.colorScheme.onSurface
		)
		Spacer(modifier = Modifier.width(16.dp))
		Text(text = title, style = MaterialTheme.typography.titleLarge)
	}
	HorizontalDivider()
}

@Composable
fun SelectionItem(
	text: String,
	isSelected: Boolean,
	onClick: () -> Unit
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.clickable(onClick = onClick)
			.padding(horizontal = 16.dp, vertical = 12.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		if (isSelected) {
			Icon(
				imageVector = Icons.Rounded.Check, contentDescription = null,
				tint = MaterialTheme.colorScheme.primary
			)
		} else {
			Spacer(modifier = Modifier.width(24.dp))
		}
		Spacer(modifier = Modifier.width(16.dp))
		Text(text = text, style = MaterialTheme.typography.bodyLarge)
	}
}