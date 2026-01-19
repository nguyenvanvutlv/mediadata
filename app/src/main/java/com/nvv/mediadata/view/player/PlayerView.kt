package com.nvv.mediadata.view.player

import android.app.PictureInPictureParams
import android.graphics.Rect
import android.os.Build
import android.util.Rational
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.AspectRatio
import androidx.compose.material.icons.rounded.Audiotrack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.PauseCircleFilled
import androidx.compose.material.icons.rounded.PictureInPicture
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.PlayCircleFilled
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Subtitles
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.PlaybackParameters
import com.nvv.mediadata.R
import com.nvv.mediadata.data.provide.rememberContext
import com.nvv.mediadata.data.provide.rememberIsInPipMode
import com.nvv.mediadata.data.provide.rememberPlayerViewModel
import com.nvv.mediadata.view.core.findActivity
import com.nvv.mediadata.view.core.padStartWith0
import com.nvv.mediadata.view.core.seek.SeekerPlayer
import kotlinx.coroutines.withTimeout
import timber.log.Timber
import kotlin.math.max
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerView(
	modifier: Modifier = Modifier,
) {
	val context = rememberContext()
	val vm = rememberPlayerViewModel()
	val player by vm.player.collectAsStateWithLifecycle()
	val state by vm.state.collectAsStateWithLifecycle()
	
	val fastForwardLabel = stringResource(R.string.fast_forward_label)
	val subtitleSettingsTitle = stringResource(R.string.subtitle_settings_title)
	val audioTrackTitle = stringResource(R.string.audio_track_title)
	val subtitleTrackTitle = stringResource(R.string.subtitle_track_title)
	var v by remember {
		mutableFloatStateOf(
			state.position.toFloat() / max(1f, state.duration.toFloat())
		)
	}
	var isSeeking by remember { mutableStateOf(false) }
	var isFastForwarding by remember { mutableStateOf(false) }
	val interactionSource = remember { MutableInteractionSource() }
	var displayPosition by remember { mutableStateOf("--:--") }
	var displayDuration by remember { mutableStateOf("--:--") }
	val videoPlayerState = rememberVideoPlayerState(hideSeconds = 5)
	var videoViewBounds by remember { mutableStateOf(Rect()) }
	val isPipMode = rememberIsInPipMode()
	var canPipMode by remember { mutableStateOf(true) }

	var showAudioTracks by remember { mutableStateOf(false) }
	var showSubtitleTracks by remember { mutableStateOf(false) }
	var showSettings by remember { mutableStateOf(false) }
	val sheetState = rememberModalBottomSheetState()

	val onPipMode = {
			val params = PictureInPictureParams.Builder()
				.setAspectRatio(Rational(16, 9))
				.setSourceRectHint(videoViewBounds)
				.build()
			if (canPipMode) {
				context.findActivity()?.enterPictureInPictureMode(params)
			}
		}
	LaunchedEffect(state.position) {
		if (!isSeeking) {
			v = state.position.toFloat() / max(1f, state.duration.toFloat())
		}
		displayPosition = state.position.milliseconds.toComponents { h, m, s, _ ->
			if (h > 0) {
				"$h:${m.padStartWith0()}:${s.padStartWith0()}"
			} else {
				"${m.padStartWith0()}:${s.padStartWith0()}"
			}
		}
		displayDuration = state.duration.milliseconds.toComponents { h, m, s, _ ->
			if (h > 0) {
				"$h:${m.padStartWith0()}:${s.padStartWith0()}"
			} else {
				"${m.padStartWith0()}:${s.padStartWith0()}"
			}
		}
	}
	LaunchedEffect(interactionSource) {
		interactionSource.interactions.collect { interaction ->
			when (interaction) {
				is DragInteraction.Start -> {
					isSeeking = true
					videoPlayerState.showControls(false)
				}

				is DragInteraction.Stop, is DragInteraction.Cancel -> {
					isSeeking = false
					videoPlayerState.showControls(true)
				}
			}
		}
	}
	LaunchedEffect(Unit) {
		videoPlayerState.showControls()
	}
	BackHandler {
		canPipMode = false
		player?.pause()
		vm.stop()
	}
	Surface(
		Modifier.fillMaxSize()
	) {
		Box(
			Modifier.fillMaxSize()
		) {
			SurfacePlayer(modifier, isPipMode)
			Box(
				Modifier
					.fillMaxSize()
					.then(
						if (videoPlayerState.isControlsVisible) {
							Modifier.background(Color.DarkGray.copy(alpha = 0.5f))
						} else {
							Modifier
						}
					)
					.pointerInput(Unit) {
						detectTapGestures(
							onTap = {
								if (videoPlayerState.isControlsVisible) {
									videoPlayerState.hideControls()
								} else {
									videoPlayerState.showControls(isPlaying = state.isPlaying)
								}
							},
							onPress = { offset ->
								val longPressTimeout = viewConfiguration.longPressTimeoutMillis
								var isLongPress = false
								try {
									withTimeout(longPressTimeout) {
										awaitRelease()
									}
								} catch (e: Exception) {
									isLongPress = true
									isFastForwarding = true
									player?.playbackParameters = PlaybackParameters(2f)
									awaitRelease()
								} finally {
									if (isLongPress) {
										isFastForwarding = false
										player?.playbackParameters = PlaybackParameters(1f)
									}
								}
							}
						)
					}
			) {
				/// X2 SPEED
				AnimatedVisibility(
					isFastForwarding,
					Modifier
						.fillMaxSize()
						.padding(top = 10.dp),
					enter =
						slideInVertically(
							initialOffsetY = { fullHeight -> -fullHeight }
						) + fadeIn(
							initialAlpha = 0.3f
						),
					exit =
						slideOutVertically(
							targetOffsetY = { fullHeight -> -fullHeight }
						)
				) {
					Box(
						modifier = Modifier.fillMaxSize(),
						contentAlignment = Alignment.TopCenter
					) {
						Text(
							text = fastForwardLabel,
							color = Color.White,
							modifier = Modifier
								.padding(top = 32.dp)
								.background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
								.padding(horizontal = 16.dp, vertical = 8.dp)
						)
					}
				}
				/// TOP BAR
				AnimatedVisibility(
					videoPlayerState.isControlsVisible && !isPipMode,
					Modifier
						.fillMaxSize(),
					enter =
						slideInVertically(
							initialOffsetY = { fullHeight -> -fullHeight }
						) + fadeIn(
							initialAlpha = 0.3f
						),
					exit =
						slideOutVertically(
							targetOffsetY = { fullHeight -> -fullHeight }
						)
				) {
					Column(
						modifier = Modifier.align(Alignment.TopCenter)
					) {
						Row(
							modifier = Modifier
								.fillMaxWidth()
								.padding(horizontal = 16.dp, vertical = 20.dp),
							verticalAlignment = Alignment.CenterVertically
						) {
							IconButton(
								onClick = {
									canPipMode = false
									player?.pause()
									vm.stop()
								}
							) {
								Icon(
									imageVector = Icons.Rounded.ArrowBackIosNew,
									contentDescription = null,
									tint = Color.White,
									modifier = Modifier
										.size(30.dp)
								)
							}
							Spacer(Modifier.weight(1f))
							IconButton(
								onClick = vm::aspect
							) {
								Icon(
									imageVector = Icons.Rounded.AspectRatio,
									contentDescription = null,
									tint = Color.White,
									modifier = Modifier
										.size(30.dp)
								)
							}
							Spacer(Modifier.width(10.dp))
							CastButton(
								modifier = Modifier.size(40.dp),
							)
							Spacer(Modifier.width(10.dp))
							IconButton(
								onClick = {
									onPipMode()
								}
							) {
								Icon(
									imageVector = Icons.Rounded.PictureInPicture,
									contentDescription = null,
									tint = Color.White,
									modifier = Modifier
										.size(30.dp)
								)
							}
						}
						Spacer(Modifier.weight(1f))
					}
				}
				/// MIDDLE
				AnimatedVisibility(
					videoPlayerState.isControlsVisible && !isPipMode,
					Modifier.fillMaxSize(),
					enter = fadeIn(),
					exit = fadeOut()
				) {
					Row(
						modifier = Modifier.fillMaxWidth(),
						horizontalArrangement = Arrangement.SpaceEvenly,
						verticalAlignment = Alignment.CenterVertically
					){
						IconButton({
							if (player?.isPlaying == true) {
								player?.pause()
							} else {
								player?.play()
							}
						}) {
							Icon(
								imageVector = if (player?.isPlaying == true) Icons.Rounded.PauseCircleFilled
								else Icons.Rounded.PlayCircleFilled,
								contentDescription = null,
								tint = Color.White,
								modifier = Modifier
									.size(80.dp)
							)
						}
					}
				}
				/// BUFFER
				AnimatedVisibility(
					state.isBuffering,
					Modifier.fillMaxSize(),
					fadeIn(),
					fadeOut()
				) {
					Row(
						modifier = Modifier.fillMaxWidth(),
						horizontalArrangement = Arrangement.SpaceEvenly,
						verticalAlignment = Alignment.CenterVertically
					) {
						CircularProgressIndicator(
							color = Color.Red.copy(alpha = 0.8f),
							modifier = Modifier.size(100.dp)
						)
					}
				}
				/// BOTTOM
				AnimatedVisibility(
					videoPlayerState.isControlsVisible && !isPipMode,
					Modifier, enter = slideInVertically(
						initialOffsetY = { fullHeight -> fullHeight }
					) + fadeIn(
						initialAlpha = 0.3f
					), exit = slideOutVertically(
						targetOffsetY = { fullHeight -> fullHeight }
					) + fadeOut()) {
					Column(
						Modifier.fillMaxSize()
					) {
						Spacer(Modifier.weight(1f))
						Row(
							modifier = Modifier.fillMaxWidth()
								.padding(horizontal = 20.dp),
							horizontalArrangement = Arrangement.SpaceBetween
						) {
							Text(
								text = displayPosition,
								color = Color.White,
								style = MaterialTheme.typography.bodySmall,
							)
							Text(
								text = displayDuration,
								color = Color.White,
								style = MaterialTheme.typography.bodySmall,
							)
						}
						SeekerPlayer(
							value = v,
							onValueChange = { v = it },
							onValueChangeFinished = {
								Timber.tag("new value").d(v.toDouble().toString())
								vm.seekTo(v.toDouble())
							},
							range = 0f..1f,
							modifier = Modifier
								.fillMaxWidth()
								.padding(horizontal = 16.dp, vertical = 10.dp),
							interactionSource = interactionSource,
						)
						Row(
							modifier = Modifier
								.fillMaxWidth()
								.padding(horizontal = 20.dp),
							horizontalArrangement = Arrangement.Start,
							verticalAlignment = Alignment.CenterVertically
						) {
							IconButton({
								showSubtitleTracks = true
							}) {
								Icon(
									imageVector = Icons.Rounded.Subtitles,
									contentDescription = null,
									tint = Color.White,
									modifier = Modifier
										.size(30.dp)
								)
							}
							Spacer(
								Modifier.width(20.dp)
							)
							IconButton({
								showAudioTracks = true
							}) {
								Icon(
									imageVector = Icons.Rounded.Audiotrack,
									contentDescription = null,
									tint = Color.White,
									modifier = Modifier
										.size(30.dp)
								)
							}
							Spacer(
								Modifier.width(20.dp)
							)
							IconButton({
								showSettings = true
							}) {
								Icon(
									imageVector = Icons.Rounded.Settings,
									contentDescription = null,
									tint = Color.White,
									modifier = Modifier
										.size(30.dp)
								)
							}
						}
						Spacer(Modifier.height(20.dp))
					}
				}
				AnimatedVisibility(
					showSettings,
					Modifier, enter = slideInVertically(
						initialOffsetY = { fullHeight -> fullHeight }
					) + fadeIn(
						initialAlpha = 0.3f
					), exit = slideOutVertically(
						targetOffsetY = { fullHeight -> fullHeight }
					) + fadeOut()){
					ModalBottomSheet(
						onDismissRequest = { showSettings = false },
						sheetState = sheetState
					) {
						Column(modifier = Modifier.padding(16.dp)) {
							Text(subtitleSettingsTitle, style = MaterialTheme.typography.titleLarge)
							Spacer(Modifier.height(16.dp))

							Text(stringResource(R.string.subtitle_size_label, state.sizeSubtitle.toInt()))
							androidx.compose.material3.Slider(
								value = state.sizeSubtitle,
								onValueChange = { vm.updateSubtitleSize(it) },
								valueRange = 10f..40f,
								modifier = Modifier.fillMaxWidth()
							)

							Spacer(Modifier.height(16.dp))

							Text(stringResource(R.string.subtitle_position_label, (state.positionSubtitle * 100).toInt()))
							androidx.compose.material3.Slider(
								value = state.positionSubtitle,
								onValueChange = { vm.updateSubtitlePosition(it) },
								valueRange = 0f..0.5f,
								modifier = Modifier.fillMaxWidth()
							)

							Spacer(Modifier.height(32.dp))
						}
					}
				}
				//// TRACK
				AnimatedVisibility(
					showAudioTracks,
					Modifier, enter = slideInVertically(
						initialOffsetY = { fullHeight -> fullHeight }
					) + fadeIn(
						initialAlpha = 0.3f
					), exit = slideOutVertically(
						targetOffsetY = { fullHeight -> fullHeight }
					) + fadeOut()){
					ModalBottomSheet(
						onDismissRequest = { showAudioTracks = false },
						sheetState = sheetState
					) {
						Column(modifier = Modifier.padding(16.dp)) {
							Text(audioTrackTitle, style = MaterialTheme.typography.titleLarge)
							Spacer(Modifier.height(16.dp))
							vm.getAudioTracks().forEachIndexed { index, track ->
								ListItem(
									headlineContent = { Text(track) },
									modifier = Modifier.pointerInput(Unit) {
										detectTapGestures(onTap = {
											vm.selectAudioTrack(index)
											showAudioTracks = false
										})
									},
								)
							}
						}
					}
				}
				AnimatedVisibility(
					showSubtitleTracks,
					Modifier, enter = slideInVertically(
						initialOffsetY = { fullHeight -> fullHeight }
					) + fadeIn(
						initialAlpha = 0.3f
					), exit = slideOutVertically(
						targetOffsetY = { fullHeight -> fullHeight }
					) + fadeOut()){
					ModalBottomSheet(
						onDismissRequest = { showSubtitleTracks = false },
						sheetState = sheetState
					) {
						Column(modifier = Modifier.padding(16.dp)) {
							Text(subtitleTrackTitle, style = MaterialTheme.typography.titleLarge)
							Spacer(Modifier.height(16.dp))

							vm.getSubtitleTracks().forEachIndexed { index, track ->
								ListItem(
									headlineContent = { Text(track) },
									modifier = Modifier.pointerInput(Unit) {
										detectTapGestures(onTap = {
											vm.selectSubtitleTrack(index)
											showSubtitleTracks = false
										})
									},
								)
							}
						}
					}
				}
			}
		}
	}
}