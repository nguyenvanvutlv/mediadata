package com.nvv.mediadata.view.player

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Audiotrack
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Subtitles
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.C
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nvv.mediadata.R
import com.nvv.mediadata.data.model.PlaybackState
import com.nvv.mediadata.data.provide.rememberContext
import com.nvv.mediadata.data.viewmodel.PlayerViewModel
import com.nvv.mediadata.data.viewmodel.Settings
import com.nvv.mediadata.ui.theme.Media3Typography
import java.util.Locale
import android.graphics.Color as AndroidColor


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
				.width(380.dp)
				.heightIn(min = 200.dp, max = 600.dp)
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
						viewModel = viewModel,
						onTrackSelected = {
							viewModel.selectSubtitleTrack(it)
							navController.popBackStack()
						},
						onBack = { navController.popBackStack() }
					)
				}
				composable("settings") {
					SubtitleSettingScreen(
						viewModel = viewModel,
						state = state,
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
	viewModel: PlayerViewModel,
	onTrackSelected: (Int) -> Unit,
	onBack: () -> Unit
) {
	val player by viewModel.player.collectAsStateWithLifecycle()
	var selectedIndex by remember { mutableStateOf(-1) }

	// Get tracks and find selected one
	val tracks = remember(player?.currentTracks) {
		val trackList = mutableListOf<Pair<String, Boolean>>()
		val currentTracks = player?.currentTracks ?: return@remember emptyList()
		var trackCount = 0
		for (group in currentTracks.groups) {
			if (group.type == C.TRACK_TYPE_TEXT) {
				for (i in 0 until group.length) {
					val format = group.getTrackFormat(i)
					val language = format.language
					val displayName = if (language != null) {
						try {
							Locale.forLanguageTag(language).getDisplayLanguage(Locale.ENGLISH)
								.replaceFirstChar { it.uppercaseChar() }
						} catch (e: Exception) {
							language.uppercase()
						}
					} else {
						"Unknown"
					}
					val isSelected = group.isTrackSelected(i)
					if (isSelected) {
						selectedIndex = trackCount
					}
					trackList.add(displayName to isSelected)
					trackCount++
				}
			}
		}
		trackList
	}

	Column {
		Header(stringResource(R.string.subtitle_track_title), onBack)
		LazyColumn(
			modifier = Modifier.fillMaxWidth()
		) {
			if (tracks.isEmpty()) {
				item {
					Text(
						text = "No subtitles available",
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.onSurfaceVariant,
						modifier = Modifier
							.fillMaxWidth()
							.padding(16.dp),
						textAlign = TextAlign.Center
					)
				}
			} else {
				itemsIndexed(tracks) { index, (track, isSelected) ->
					SelectionItem(
						text = track,
						isSelected = isSelected,
						onClick = { onTrackSelected(index) }
					)
				}
			}
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubtitleSettingScreen(
	viewModel: PlayerViewModel,
	state: PlaybackState,
	onBack: () -> Unit
) {
	val context = rememberContext()

	var subtitleSize by remember { mutableFloatStateOf(state.sizeSubtitle) }
	var verticalOffset by remember { mutableFloatStateOf(state.positionSubtitle * 100f) }
	var textColor by remember { mutableIntStateOf(state.subtitleTextColor) }
	var outlineColor by remember { mutableIntStateOf(state.subtitleOutlineColor) }
	var backgroundColor by remember { mutableIntStateOf(state.subtitleBackgroundColor) }
	var subtitleFont by remember { mutableStateOf(state.subtitleFont) }

	var isFontExpanded by remember { mutableStateOf(false) }
	var isTextColorPickerOpen by remember { mutableStateOf(false) }
	var isOutlineColorPickerOpen by remember { mutableStateOf(false) }
	var isBackgroundColorPickerOpen by remember { mutableStateOf(false) }

	val colorOptions = listOf(
		"White" to androidx.compose.ui.graphics.Color.White,
		"Black" to androidx.compose.ui.graphics.Color.Black,
		"Red" to androidx.compose.ui.graphics.Color.Red,
		"Yellow" to androidx.compose.ui.graphics.Color.Yellow,
		"Blue" to androidx.compose.ui.graphics.Color.Blue,
		"Green" to androidx.compose.ui.graphics.Color.Green,
		"Magenta" to androidx.compose.ui.graphics.Color.Magenta,
		"Transparent" to androidx.compose.ui.graphics.Color.Transparent
	)

	LaunchedEffect(subtitleSize) {
		viewModel.updateSubtitleSize(subtitleSize)
	}
	LaunchedEffect(verticalOffset) {
		viewModel.updateSubtitlePosition(verticalOffset / 100f)
	}
	LaunchedEffect(textColor) {
		Settings.setColor(context, AndroidColor.valueOf(textColor))
		viewModel.updateSubtitleSettings()
	}
	LaunchedEffect(outlineColor) {
		Settings.setSubtitleOutlineColor(context, outlineColor)
		viewModel.updateSubtitleSettings()
	}
	LaunchedEffect(backgroundColor) {
		Settings.setSubtitleBackgroundColor(context, backgroundColor)
		viewModel.updateSubtitleSettings()
	}
	LaunchedEffect(subtitleFont) {
		Settings.setSubtitleFont(context, subtitleFont)
		viewModel.updateSubtitleSettings()
	}

	Column {
		Header(stringResource(R.string.subtitle_settings_title), onBack)
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.verticalScroll(rememberScrollState())
				.padding(horizontal = 16.dp, vertical = 8.dp)
		) {
			// Size
			SettingRow(
				label = "Size",
				value = "${subtitleSize.toInt()}%",
				onDecrease = {
					if (subtitleSize > 10f) subtitleSize -= 5f
				},
				onIncrease = {
					if (subtitleSize < 200f) subtitleSize += 5f
				}
			)
			Spacer(modifier = Modifier.height(12.dp))

			// Vertical Offset
			SettingRow(
				label = "Vertical Offset",
				value = "${verticalOffset.toInt()}%",
				onDecrease = {
					if (verticalOffset > 0f) verticalOffset -= 5f
				},
				onIncrease = {
					if (verticalOffset < 50f) verticalOffset += 5f
				}
			)
			Spacer(modifier = Modifier.height(12.dp))

			// Font
			Text(
				text = "Font",
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurfaceVariant,
				modifier = Modifier.padding(bottom = 8.dp)
			)
			FontDropdown(
				selectedFont = subtitleFont,
				fonts = Media3Typography.FontFamilyType.entries.map {
					it.name.replace("_", " ") to it.name
				},
				expanded = isFontExpanded,
				onExpandedChange = { isFontExpanded = it },
				onFontSelected = { font ->
					subtitleFont = font
					Settings.setSubtitleFont(context, font)
					isFontExpanded = false
				}
			)
			Spacer(modifier = Modifier.height(12.dp))

			// Text Color
			ColorPickerRow(
				label = "Text Color",
				color = textColor,
				onClick = { isTextColorPickerOpen = true }
			)
			Spacer(modifier = Modifier.height(12.dp))

			// Outline Color
			ColorPickerRow(
				label = "Outline Color",
				color = outlineColor,
				onClick = { isOutlineColorPickerOpen = true }
			)
			Spacer(modifier = Modifier.height(12.dp))

			// Background Color
			ColorPickerRow(
				label = "Background Color",
				color = backgroundColor,
				onClick = { isBackgroundColorPickerOpen = true }
			)
		}
	}

	// Color picker dialogs
	ColorPickerDialog(
		colors = colorOptions,
		expanded = isTextColorPickerOpen,
		onDismiss = { isTextColorPickerOpen = false },
		onColorSelected = { color ->
			textColor = color.toArgb()
			isTextColorPickerOpen = false
		}
	)

	ColorPickerDialog(
		colors = colorOptions,
		expanded = isOutlineColorPickerOpen,
		onDismiss = { isOutlineColorPickerOpen = false },
		onColorSelected = { color ->
			outlineColor = color.toArgb()
			isOutlineColorPickerOpen = false
		}
	)

	ColorPickerDialog(
		colors = colorOptions,
		expanded = isBackgroundColorPickerOpen,
		onDismiss = { isBackgroundColorPickerOpen = false },
		onColorSelected = { color ->
			backgroundColor = color.toArgb()
			isBackgroundColorPickerOpen = false
		}
	)
}

@Composable
fun Header(title: String, onBack: () -> Unit) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.padding(16.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		IconButton(onClick = onBack) {
			Icon(
				imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
				contentDescription = "Back",
				tint = MaterialTheme.colorScheme.onSurface
			)
		}
		Spacer(modifier = Modifier.width(8.dp))
		Text(
			text = title,
			style = MaterialTheme.typography.titleLarge,
			color = MaterialTheme.colorScheme.onSurface
		)
	}
	HorizontalDivider()
}

// Helper components from SubtitleEditorScreen
@Composable
fun SettingRow(
	label: String,
	value: String,
	onDecrease: () -> Unit,
	onIncrease: () -> Unit
) {
	Row(
		modifier = Modifier.fillMaxWidth(),
		horizontalArrangement = Arrangement.SpaceBetween,
		verticalAlignment = Alignment.CenterVertically
	) {
		Text(
			text = label,
			style = MaterialTheme.typography.bodyLarge
		)
		Row(
			horizontalArrangement = Arrangement.Start,
			verticalAlignment = Alignment.CenterVertically
		) {
			IconButton(
				onClick = onDecrease,
				modifier = Modifier
					.size(36.dp)
					.background(
						MaterialTheme.colorScheme.surfaceVariant,
						CircleShape
					)
			) {
				Icon(
					imageVector = Icons.Rounded.Remove,
					contentDescription = "Decrease",
					modifier = Modifier.size(20.dp)
				)
			}
			Spacer(modifier = Modifier.width(8.dp))
			Text(
				text = value,
				style = MaterialTheme.typography.bodyLarge,
				modifier = Modifier.width(50.dp),
				textAlign = TextAlign.Center
			)
			Spacer(modifier = Modifier.width(8.dp))
			IconButton(
				onClick = onIncrease,
				modifier = Modifier
					.size(36.dp)
					.background(
						MaterialTheme.colorScheme.surfaceVariant,
						CircleShape
					)
			) {
				Icon(
					imageVector = Icons.Rounded.Add,
					contentDescription = "Increase",
					modifier = Modifier.size(20.dp)
				)
			}
		}
	}
}

@Composable
fun FontDropdown(
	selectedFont: String,
	fonts: List<Pair<String, String>>,
	expanded: Boolean,
	onExpandedChange: (Boolean) -> Unit,
	onFontSelected: (String) -> Unit
) {
	val selectedDisplayName = fonts.find { it.second == selectedFont }?.first ?: "Default"
	Box {
		Card(
			modifier = Modifier
				.fillMaxWidth()
				.clickable { onExpandedChange(true) },
			colors = CardDefaults.cardColors(
				containerColor = MaterialTheme.colorScheme.surfaceVariant
			),
			shape = RoundedCornerShape(8.dp)
		) {
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.padding(horizontal = 16.dp, vertical = 12.dp),
				horizontalArrangement = Arrangement.SpaceBetween,
				verticalAlignment = Alignment.CenterVertically
			) {
				Text(
					text = selectedDisplayName,
					style = MaterialTheme.typography.bodyLarge
				)
				Icon(
					imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
					contentDescription = null,
					modifier = Modifier
						.size(16.dp)
						.rotate(90f)
				)
			}
		}

		DropdownMenu(
			expanded = expanded,
			onDismissRequest = { onExpandedChange(false) },
			modifier = Modifier.fillMaxWidth()
		) {
			fonts.forEach { (displayName, fontName) ->
				DropdownMenuItem(
					text = { Text(displayName) },
					onClick = {
						onFontSelected(fontName)
					}
				)
			}
		}
	}
}

@Composable
fun ColorPickerRow(
	label: String,
	color: Int,
	onClick: () -> Unit
) {
	Row(
		modifier = Modifier.fillMaxWidth(),
		horizontalArrangement = Arrangement.SpaceBetween,
		verticalAlignment = Alignment.CenterVertically
	) {
		Text(
			text = label,
			style = MaterialTheme.typography.bodyLarge
		)
		Card(
			modifier = Modifier
				.size(48.dp, 32.dp)
				.clickable(onClick = onClick),
			colors = CardDefaults.cardColors(
				containerColor = androidx.compose.ui.graphics.Color(color)
			),
			shape = RoundedCornerShape(8.dp),
			border = if (androidx.compose.ui.graphics.Color(color).alpha < 0.1f) {
				BorderStroke(
					1.dp,
					MaterialTheme.colorScheme.outline
				)
			} else null
		) {}
	}
}

@Composable
fun ColorPickerDialog(
	colors: List<Pair<String, androidx.compose.ui.graphics.Color>>,
	expanded: Boolean,
	onDismiss: () -> Unit,
	onColorSelected: (androidx.compose.ui.graphics.Color) -> Unit
) {
	DropdownMenu(
		expanded = expanded,
		onDismissRequest = onDismiss
	) {
		colors.forEach { (name, color) ->
			DropdownMenuItem(
				text = { Text(name) },
				leadingIcon = {
					Box(
						modifier = Modifier
							.size(24.dp)
							.background(
								color = color,
								shape = CircleShape
							)
					)
				},
				onClick = {
					onColorSelected(color)
				}
			)
		}
	}
}

@Composable
fun SelectionItem(
	text: String,
	isSelected: Boolean,
	onClick: () -> Unit
) {
	Card(
		modifier = Modifier
			.fillMaxWidth()
			.padding(horizontal = 12.dp, vertical = 6.dp)
			.clickable(onClick = onClick),
		colors = CardDefaults.cardColors(
			containerColor = if (isSelected) {
				MaterialTheme.colorScheme.primaryContainer
			} else {
				MaterialTheme.colorScheme.surfaceVariant
			}
		),
		shape = RoundedCornerShape(8.dp)
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 16.dp, vertical = 14.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			if (isSelected) {
				Icon(
					imageVector = Icons.Rounded.Check,
					contentDescription = null,
					tint = MaterialTheme.colorScheme.primary,
					modifier = Modifier.size(24.dp)
				)
				Spacer(modifier = Modifier.width(12.dp))
			}
			Text(
				text = text,
				style = MaterialTheme.typography.bodyLarge,
				color = if (isSelected) {
					MaterialTheme.colorScheme.onPrimaryContainer
				} else {
					MaterialTheme.colorScheme.onSurfaceVariant
				}
			)
		}
	}
}