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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.PictureInPicture
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.PlaybackParameters
import com.nvv.mediadata.data.provide.rememberContext
import com.nvv.mediadata.data.provide.rememberIsInPipMode
import com.nvv.mediadata.data.provide.rememberPlayerViewModel
import com.nvv.mediadata.view.core.findActivity
import com.nvv.mediadata.view.core.padStartWith0
import com.nvv.mediadata.view.core.seek.SeekerPlayer
import kotlin.math.max
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerView(
	modifier: Modifier = Modifier,
	onBack: () -> Unit,
){
	val context = rememberContext()
	val vm = rememberPlayerViewModel()
	val player by vm.player.collectAsStateWithLifecycle()
	val state by vm.state.collectAsStateWithLifecycle()
	var v by remember { mutableFloatStateOf(
		state.position.toFloat() / max(1f, state.duration.toFloat())) }
	var isSeeking by remember { mutableStateOf(false) }
	var isFastForwarding by remember { mutableStateOf(false) }
	val interactionSource = remember { MutableInteractionSource() }
	var displayPosition by remember { mutableStateOf("--:--") }
	var displayDuration by remember { mutableStateOf("--:--") }
	val videoPlayerState = rememberVideoPlayerState(hideSeconds = 5)
	var videoViewBounds by remember { mutableStateOf(Rect()) }
	val isPipMode = rememberIsInPipMode()
	var canPipMode by remember { mutableStateOf(true) }
	val onPipMode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
		{
			val params = PictureInPictureParams.Builder()
				.setAspectRatio(Rational(16, 9))
				.setSourceRectHint(videoViewBounds)
				.build()
			if (canPipMode) {
				context.findActivity()?.enterPictureInPictureMode(params)
			}
		}
	} else {
		{

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
	BackHandler() {
		canPipMode = false
	}
	Surface(
		Modifier.fillMaxSize()
	) {
		Box(
			Modifier.fillMaxSize()
		) {
			SurfacePlayer(modifier) {
				onPipMode()
			}
			Box(
				Modifier
					.fillMaxSize()
					.pointerInput(Unit) {
						awaitEachGesture {
							val down = awaitFirstDown()
							val longPressTimeout = viewConfiguration.longPressTimeoutMillis
							var isLongPressTriggered = false
							val changeSpeedJob = try {
								withTimeout(longPressTimeout) {
									waitForUpOrCancellation()
								}
							} catch (e: Exception) {
								isLongPressTriggered = true
								isFastForwarding = true
								player.playbackParameters = PlaybackParameters(2f)
								while (true) {
									val event = awaitPointerEvent()
									if (event.changes.any { it.changedToUp() }) {
										break
									}
								}
							}
							if (isLongPressTriggered) {
								isFastForwarding = false
								player.playbackParameters = PlaybackParameters(1f)
							}
						}
					}
					.pointerInput(Unit) {
						detectTapGestures(
							onTap = {
								if (videoPlayerState.isControlsVisible) {
									videoPlayerState.hideControls()
									return@detectTapGestures
								}
								videoPlayerState.showControls(isPlaying = state.isPlaying)
							},
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
							text = "2X Speed >>",
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
									player.pause()
									vm.stop()
									onBack()
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
				AnimatedVisibility(videoPlayerState.isControlsVisible && !isPipMode,
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
							modifier = Modifier
								.fillMaxWidth()
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
								vm.seekTo(v.toDouble())
							},
							range = 0f..1f,
							modifier = Modifier
								.fillMaxWidth()
								.padding(horizontal = 16.dp, vertical = 10.dp),
							interactionSource = interactionSource,
						)
						Spacer(Modifier.height(20.dp))
					}
				}
			}
		}
	}
}