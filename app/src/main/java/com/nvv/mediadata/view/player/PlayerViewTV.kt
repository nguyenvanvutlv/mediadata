package com.nvv.mediadata.view.player

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.C
import com.nvv.mediadata.data.model.PlaybackState
import com.nvv.mediadata.data.provide.rememberContext
import com.nvv.mediadata.data.provide.rememberPlayerViewModel
import com.nvv.mediadata.data.viewmodel.PlayerViewModel
import com.nvv.mediadata.data.viewmodel.Settings
import com.nvv.mediadata.ui.theme.Media3Typography
import com.nvv.mediadata.view.core.padStartWith0
import com.nvv.mediadata.view.core.seek.SeekerPlayer
import com.nvv.mediadata.view.core.tv.BaseItem
import com.nvv.mediadata.view.core.tv.detectHandleDpadKeyEvents
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.time.Duration.Companion.milliseconds
import android.graphics.Color as AndroidColor

private const val SEEK_STEP_MS = 10_000L
private const val SCRUB_STEP_FRACTION = 0.01f


@Composable
fun PlayerViewTV(
	modifier: Modifier = Modifier,
	onBack: () -> Unit = {},
) {
	val seekFocusRequester = remember { FocusRequester() }
	val scope = rememberCoroutineScope()
	val vm = rememberPlayerViewModel()
	val player by vm.player.collectAsStateWithLifecycle()
	val state by vm.state.collectAsStateWithLifecycle()
	val videoPlayerState = rememberVideoPlayerState(hideSeconds = 5)
	var progress by remember {
		mutableFloatStateOf(
			state.position.toFloat() / max(1f, state.duration.toFloat())
		)
	}
	var isSeeking by remember { mutableStateOf(false) }
	var showSettingsPanel by remember { mutableStateOf(false) }
	val seekInteractionSource = remember { MutableInteractionSource() }
	var displayPosition by remember {
		mutableStateOf(
			"0:00"
		)
	}
	var displayTotal by remember {
		mutableStateOf(
			"0:00"
		)
	}
	var currentTitle by remember {
		mutableStateOf(
			"0:00"
		)
	}
	LaunchedEffect(player?.currentMediaItem) {
		currentTitle = player?.currentMediaItem?.mediaMetadata?.title?.toString()
			?: player?.currentMediaItem?.mediaMetadata?.displayTitle?.toString()
					?: "0:00"
	}
	LaunchedEffect(state.position, state.duration) {
		if (!isSeeking) {
			progress = state.position.toFloat() / max(1f, state.duration.toFloat())
		}
		displayPosition = state.position.milliseconds.toComponents { h, m, s, _ ->
			if (h > 0)
				"$h:${m.padStartWith0()}:${s.padStartWith0()}"
			else "${m.padStartWith0()}:${s.padStartWith0()}"
		}
		displayTotal = state.duration.milliseconds.toComponents { h, m, s, _ ->
			if (h > 0)
				"$h:${m.padStartWith0()}:${s.padStartWith0()}"
			else
				"${m.padStartWith0()}:${s.padStartWith0()}"
		}
	}
	BackHandler {
		onBack()
	}
	Surface(modifier = modifier.fillMaxSize()) {
		Box(Modifier.fillMaxSize()) {
			SurfacePlayer(Modifier, isPipMode = false)
			Box(
				Modifier
					.fillMaxSize()
					.then(
						if (videoPlayerState.isControlsVisible) {
							Modifier.background(Color.Black.copy(alpha = 0.4f))
						} else Modifier
					)
					.focusable()
					.detectHandleDpadKeyEvents(
						onPressed = {
							if (showSettingsPanel) {
								return@detectHandleDpadKeyEvents
							}
							if (!videoPlayerState.isControlsVisible) {
								videoPlayerState.showControls(
									isPlaying = player?.isPlaying == true
								)
								scope.launch {
									delay(300L)
									seekFocusRequester.requestFocus()
								}
							}
						}
					)
			) {
				AnimatedVisibility(
					visible = videoPlayerState.isControlsVisible,
					modifier = Modifier.fillMaxSize(),
					enter = slideInVertically { -it } + fadeIn(),
					exit = slideOutVertically { -it } + fadeOut()
				) {
					Column(Modifier.fillMaxSize()) {
						TopBarTV(
							title = currentTitle,
							onSettingsClick = { showSettingsPanel = true },
							contentDescriptionSettings = "Settings",
							modifier = Modifier
								.onFocusChanged { state ->
									if (state.isFocused) {
										videoPlayerState.showControls()
									}
								}
						)
						Spacer(Modifier.weight(1f))
					}
				}
				AnimatedVisibility(
					visible = state.isBuffering && !state.isError,
					modifier = Modifier.fillMaxSize(),
					enter = fadeIn(),
					exit = fadeOut()
				) {
					Box(
						Modifier.fillMaxSize(),
						contentAlignment = Alignment.Center
					) {
						CircularProgressIndicator(
							color = Color.White,
							modifier = Modifier.size(64.dp)
						)
					}
				}
				AnimatedVisibility(
					visible = videoPlayerState.isControlsVisible,
					modifier = Modifier.fillMaxSize(),
					enter = slideInVertically { it } + fadeIn(),
					exit = slideOutVertically { it } + fadeOut()
				) {
					Column(
						Modifier
							.fillMaxWidth()
							.align(Alignment.BottomCenter)
							.padding(horizontal = 24.dp, vertical = 24.dp)
					) {
						Spacer(
							Modifier.weight(1f)
						)
						Row(
							modifier = Modifier.fillMaxWidth(),
							horizontalArrangement = Arrangement.Center,
							verticalAlignment = Alignment.CenterVertically
						) {
							Text(
								text = "$displayPosition / $displayTotal",
								color = Color.White,
								style = MaterialTheme.typography.bodyMedium,
								modifier = Modifier.semantics { contentDescription = "Elapsed time $displayPosition" }
							)
							SeekBarViewTV(
								value = progress,
								onValueChange = {
									progress = it
									isSeeking = true
									videoPlayerState.showControls(false)
								},
								onValueChangeFinished = {
									vm.seekTo(progress.toDouble())
									isSeeking = false
									videoPlayerState.showControls(true)
								},
								interactionSource = seekInteractionSource,
								modifier = Modifier
									.onFocusChanged { state ->
										if (state.isFocused) {
											videoPlayerState.showControls()
										}
									}
									.focusRequester(seekFocusRequester)
									.semantics { contentDescription = "Seek bar. Use left and right to scrub." }
							)

						}
					}
				}
			}
			if (showSettingsPanel) {
				LaunchedEffect(Unit) {
					videoPlayerState.hideControls()
				}
				BackHandler { showSettingsPanel = false }
				SettingsPanelTV(
					viewModel = vm,
					onDismiss = { showSettingsPanel = false }
				)
			}
		}
	}
	LaunchedEffect(Unit) {
		videoPlayerState.showControls()
		seekFocusRequester.requestFocus()
	}
}


@Composable
private fun TopBarTV(
	title: String,
	onSettingsClick: () -> Unit,
	contentDescriptionSettings: String,
	modifier: Modifier = Modifier,
) {
	Row(
		modifier = modifier
			.fillMaxWidth()
			.padding(horizontal = 24.dp, vertical = 20.dp),
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.SpaceBetween
	) {
		Text(
			text = title.ifEmpty { "" },
			color = Color.White,
			style = MaterialTheme.typography.titleLarge,
			maxLines = 1,
			modifier = Modifier
				.weight(1f)
				.semantics { contentDescription = "Title: $title" }
		)
		BaseItem(
			title = "Settings",
			onClick = onSettingsClick,
			modifier = Modifier
				.widthIn(max = 180.dp)
				.padding(start = 16.dp)
				.semantics { contentDescription = contentDescriptionSettings },
			leadingContent = {
				Icon(
					imageVector = Icons.Rounded.Settings,
					contentDescription = null,
					modifier = Modifier.size(24.dp),
					tint = Color.White
				)
			}
		)
	}
}


@Composable
private fun SeekBarViewTV(
	value: Float,
	onValueChange: (Float) -> Unit,
	onValueChangeFinished: () -> Unit,
	interactionSource: MutableInteractionSource,
	modifier: Modifier = Modifier,
) {
	val coerced = value.coerceIn(0f, 1f)
	SeekerPlayer(
		value = coerced,
		onValueChange = onValueChange,
		onValueChangeFinished = onValueChangeFinished,
		range = 0f..1f,
		modifier = modifier
			.height(48.dp)
			.onPreviewKeyEvent { keyEvent ->
				if (keyEvent.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
				when (keyEvent.key) {
					Key.DirectionLeft -> {
						onValueChange((coerced - SCRUB_STEP_FRACTION).coerceIn(0f, 1f))
						onValueChangeFinished()
						true
					}

					Key.DirectionRight -> {
						onValueChange((coerced + SCRUB_STEP_FRACTION).coerceIn(0f, 1f))
						onValueChangeFinished()
						true
					}

					else -> false
				}
			},
		interactionSource = interactionSource
	)
}


@Composable
private fun SettingsPanelTV(
	viewModel: PlayerViewModel,
	onDismiss: () -> Unit,
	modifier: Modifier = Modifier,
) {
	val contentFocusRequester = remember { FocusRequester() }
	val state by viewModel.state.collectAsStateWithLifecycle()
	var selectedTabIndex by remember { mutableIntStateOf(0) }
	val tabs = listOf("Settings", "Subtitle", "Audio")
	LaunchedEffect(Unit) {
		contentFocusRequester.requestFocus()
	}
	Box(
		modifier = modifier
			.fillMaxSize()
			.background(Color.Black.copy(alpha = 0.8f))
			.onPreviewKeyEvent { keyEvent ->
				if (keyEvent.type == KeyEventType.KeyDown &&
					(keyEvent.key == Key.Back || keyEvent.key == Key.Escape)
				) {
					onDismiss()
					true
				} else false
			}
			.focusable()
			.focusRequester(contentFocusRequester)
	) {
		Surface(
			modifier = Modifier
				.widthIn(max = 480.dp)
				.heightIn(max = 560.dp)
				.padding(48.dp)
				.align(Alignment.Center),
			shape = RoundedCornerShape(16.dp),
			color = MaterialTheme.colorScheme.surface
		) {
			Column(modifier = Modifier.padding(24.dp)) {
				Row(
					modifier = Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.spacedBy(8.dp)
				) {
					tabs.forEachIndexed { index, label ->
						val isSelected = selectedTabIndex == index
						BaseItem(
							title = label,
							onClick = { selectedTabIndex = index },
							isSelected = isSelected,
							modifier = Modifier.weight(1f)
						)
					}
				}
				Spacer(Modifier.height(16.dp))
				when (selectedTabIndex) {
					0 -> SettingsTabContentTV(viewModel = viewModel, state = state)
					1 -> SubtitleTabContentTV(viewModel = viewModel, onDismiss = onDismiss)
					2 -> AudioTabContentTV(viewModel = viewModel, onDismiss = onDismiss)
				}
				Spacer(Modifier.height(16.dp))
				BaseItem(
					title = "Close",
					onClick = onDismiss,
					modifier = Modifier.fillMaxWidth()
				)
			}
		}
	}
}


private sealed class ExpandedSetting {
	data object Size : ExpandedSetting()
	data object VerticalOffset : ExpandedSetting()
	data object Font : ExpandedSetting()
	data object TextColor : ExpandedSetting()
	data object OutlineColor : ExpandedSetting()
	data object BackgroundColor : ExpandedSetting()
}

@Composable
private fun SettingsTabContentTV(
	viewModel: PlayerViewModel,
	state: PlaybackState,
	modifier: Modifier = Modifier,
) {
	val context = rememberContext()
	var expandedSetting by remember { mutableStateOf<ExpandedSetting?>(null) }
	val listFocusRequester = remember { FocusRequester() }
	val panelFocusRequester = remember { FocusRequester() }
	LaunchedEffect(expandedSetting) {
		if (expandedSetting != null) {
			panelFocusRequester.requestFocus()
		} else {
			listFocusRequester.requestFocus()
		}
	}
	BackHandler(enabled = expandedSetting != null) {
		expandedSetting = null
	}

	Box(
		modifier = modifier
			.fillMaxWidth()
			.heightIn(max = 320.dp)
	) {
		LazyColumn(
			modifier = Modifier
				.fillMaxWidth()
				.focusGroup(),
			verticalArrangement = Arrangement.spacedBy(4.dp)
		) {
			item {
				BaseItem(
					title = "Size",
					subtitle = "${state.sizeSubtitle.toInt()}%",
					onClick = { expandedSetting = ExpandedSetting.Size },
					modifier = Modifier.focusRequester(listFocusRequester)
				)
			}
			item {
				BaseItem(
					title = "Vertical Offset",
					subtitle = "${(state.positionSubtitle * 100).toInt()}%",
					onClick = { expandedSetting = ExpandedSetting.VerticalOffset }
				)
			}
			item {
				BaseItem(
					title = "Font",
					subtitle = state.subtitleFont.replace("_", " "),
					onClick = { expandedSetting = ExpandedSetting.Font }
				)
			}
			item {
				BaseItem(
					title = "Text Color",
					subtitle = colorToLabel(state.subtitleTextColor),
					onClick = { expandedSetting = ExpandedSetting.TextColor }
				)
			}
			item {
				BaseItem(
					title = "Outline Color",
					subtitle = colorToLabel(state.subtitleOutlineColor),
					onClick = { expandedSetting = ExpandedSetting.OutlineColor }
				)
			}
			item {
				BaseItem(
					title = "Background Color",
					subtitle = colorToLabel(state.subtitleBackgroundColor),
					onClick = { expandedSetting = ExpandedSetting.BackgroundColor }
				)
			}
		}
		if (expandedSetting != null) {
			LaunchedEffect(Unit) {
				delay(300L)
				panelFocusRequester.requestFocus()
			}
			Box(
				modifier = Modifier
					.fillMaxSize()
					.background(Color.Black.copy(alpha = 0.6f))
					.focusable()
					.focusGroup()
			) {
				when (expandedSetting) {
					ExpandedSetting.Size -> {
						val options = (10..200 step 5).map { it to "${it}%" }
						LazyColumn(
							modifier = Modifier
								.fillMaxSize()
								.focusRequester(panelFocusRequester)
								.padding(horizontal = 24.dp, vertical = 16.dp),
							verticalArrangement = Arrangement.spacedBy(4.dp)
						) {
							items(options.size) { i ->
								val (value, label) = options[i]
								BaseItem(
									title = label,
									onClick = {
										viewModel.updateSubtitleSize(value.toFloat())
										expandedSetting = null
									},
									isSelected = state.sizeSubtitle.toInt() == value,
									modifier = Modifier.fillMaxWidth()
								)
							}
						}
					}

					ExpandedSetting.VerticalOffset -> {
						val options = (0..50 step 5).map { it to "${it}%" }
						LazyColumn(
							modifier = Modifier
								.fillMaxSize()
								.focusRequester(panelFocusRequester)
								.padding(horizontal = 24.dp, vertical = 16.dp),
							verticalArrangement = Arrangement.spacedBy(4.dp)
						) {
							items(options.size) { i ->
								val (value, label) = options[i]
								BaseItem(
									title = label,
									onClick = {
										viewModel.updateSubtitlePosition(value / 100f)
										expandedSetting = null
									},
									isSelected = (state.positionSubtitle * 100).toInt() == value,
									modifier = Modifier.fillMaxWidth()
								)
							}
						}
					}

					ExpandedSetting.Font -> {
						val fonts = Media3Typography.FontFamilyType.entries
						LazyColumn(
							modifier = Modifier
								.fillMaxSize()
								.focusRequester(panelFocusRequester)
								.padding(horizontal = 24.dp, vertical = 16.dp),
							verticalArrangement = Arrangement.spacedBy(4.dp)
						) {
							items(fonts.size) { i ->
								val fontType = fonts[i]
								val name = fontType.name.replace("_", " ")
								BaseItem(
									title = name,
									onClick = {
										Settings.setSubtitleFont(context, fontType.name)
										viewModel.updateSubtitleSettings()
										expandedSetting = null
									},
									isSelected = state.subtitleFont == fontType.name,
									modifier = Modifier.fillMaxWidth()
								)
							}
						}
					}

					ExpandedSetting.TextColor -> {
						val colors = subtitleColorOptions()
						LazyColumn(
							modifier = Modifier
								.fillMaxSize()
								.focusRequester(panelFocusRequester)
								.padding(horizontal = 24.dp, vertical = 16.dp),
							verticalArrangement = Arrangement.spacedBy(4.dp)
						) {
							items(colors.size) { i ->
								val (label, color) = colors[i]
								val argb = color.toArgb()
								BaseItem(
									title = label,
									onClick = {
										Settings.setColor(context, AndroidColor.valueOf(argb))
										viewModel.updateSubtitleSettings()
										expandedSetting = null
									},
									isSelected = state.subtitleTextColor == argb,
									modifier = Modifier.fillMaxWidth()
								)
							}
						}
					}

					ExpandedSetting.OutlineColor -> {
						val colors = subtitleColorOptions()
						LazyColumn(
							modifier = Modifier
								.fillMaxSize()
								.focusRequester(panelFocusRequester)
								.padding(horizontal = 24.dp, vertical = 16.dp),
							verticalArrangement = Arrangement.spacedBy(4.dp)
						) {
							items(colors.size) { i ->
								val (label, color) = colors[i]
								val argb = color.toArgb()
								BaseItem(
									title = label,
									onClick = {
										Settings.setSubtitleOutlineColor(context, argb)
										viewModel.updateSubtitleSettings()
										expandedSetting = null
									},
									isSelected = state.subtitleOutlineColor == argb,
									modifier = Modifier.fillMaxWidth()
								)
							}
						}
					}

					ExpandedSetting.BackgroundColor -> {
						val colors = subtitleColorOptions()
						LazyColumn(
							modifier = Modifier
								.fillMaxSize()
								.focusRequester(panelFocusRequester)
								.padding(horizontal = 24.dp, vertical = 16.dp),
							verticalArrangement = Arrangement.spacedBy(4.dp)
						) {
							items(colors.size) { i ->
								val (label, color) = colors[i]
								val argb = color.toArgb()
								BaseItem(
									title = label,
									onClick = {
										Settings.setSubtitleBackgroundColor(context, argb)
										viewModel.updateSubtitleSettings()
										expandedSetting = null
									},
									isSelected = state.subtitleBackgroundColor == argb,
									modifier = Modifier.fillMaxWidth()
								)
							}
						}
					}

					null -> {}
				}
			}
		}
	}
}

private fun subtitleColorOptions(): List<Pair<String, Color>> = listOf(

	"White" to Color.White,
	"Black" to Color.Black,
	"Red" to Color.Red,
	"Yellow" to Color.Yellow,
	"Blue" to Color.Blue,
	"Green" to Color.Green,
	"Magenta" to Color.Magenta,
	"Transparent" to Color.Transparent
)

private fun colorToLabel(argb: Int): String {
	return subtitleColorOptions().find { it.second.toArgb() == argb }?.first ?: "Custom"
}

@Composable
private fun SubtitleTabContentTV(
	viewModel: PlayerViewModel,
	onDismiss: () -> Unit,
	modifier: Modifier = Modifier,
) {
	val player by viewModel.player.collectAsStateWithLifecycle()
	val tracks = remember(player?.currentTracks) {
		val list = mutableListOf<Pair<String, Boolean>>()
		val currentTracks = player?.currentTracks ?: return@remember emptyList()
		for (group in currentTracks.groups) {
			if (group.type == C.TRACK_TYPE_TEXT) {
				for (i in 0 until group.length) {
					val format = group.getTrackFormat(i)
					val lang = format.language ?: "Unknown"
					list.add(lang to group.isTrackSelected(i))
				}
			}
		}
		list
	}
	LazyColumn(
		modifier = modifier
			.fillMaxWidth()
			.heightIn(max = 320.dp),
		verticalArrangement = Arrangement.spacedBy(4.dp)
	) {
		if (tracks.isEmpty()) {
			item {
				BaseItem(title = "No subtitles available", onClick = { })
			}
		} else {
			itemsIndexed(tracks) { index, (name, isSelected) ->
				BaseItem(
					title = name,
					onClick = {
						viewModel.selectSubtitleTrack(index)
						onDismiss()
					},
					isSelected = isSelected
				)
			}
		}
	}
}

@Composable
private fun AudioTabContentTV(
	viewModel: PlayerViewModel,
	onDismiss: () -> Unit,
	modifier: Modifier = Modifier,
) {
	val tracks = remember { viewModel.getAudioTracks() }
	LazyColumn(
		modifier = modifier
			.fillMaxWidth()
			.heightIn(max = 320.dp),
		verticalArrangement = Arrangement.spacedBy(4.dp)
	) {
		if (tracks.isEmpty()) {
			item {
				BaseItem(title = "No audio tracks available", onClick = { })
			}
		} else {
			itemsIndexed(tracks) { index, name ->
				BaseItem(
					title = name,
					onClick = {
						viewModel.selectAudioTrack(index)
						onDismiss()
					}
				)
			}
		}
	}
}
