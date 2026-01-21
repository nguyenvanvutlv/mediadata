package com.nvv.mediadata.data.viewmodel

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.nvv.mediadata.data.model.PlaybackState
import com.nvv.mediadata.data.model.VideoScaleMode
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber
import java.io.File
import javax.inject.Inject

@OptIn(UnstableApi::class)
@HiltViewModel
class PlayerViewModel @Inject constructor(
	@ApplicationContext private val context: Context,
) : ViewModel() {
	private var _isPlay = MutableStateFlow(false)
	val isPlay = _isPlay.asStateFlow()

	private val _player = MutableStateFlow<Player?>(null)
	val player = _player.asStateFlow()

	private val _state = MutableStateFlow(PlaybackState())
	val state = _state.asStateFlow()

	private val _index = MutableStateFlow(0)
	val currentMediaIndex = _index.asStateFlow()

	private val _items = MutableStateFlow<List<MediaItem>>(emptyList())

	private val handler = Handler(Looper.getMainLooper())
	private var controllerFuture: ListenableFuture<MediaController>

	private fun String.toMediaUri(): Uri {
		return when {
			startsWith("http://", true) ||
					startsWith("https://", true) ||
					startsWith("content://", true) ||
					startsWith("file://", true) ->
				this.toUri()

			else ->
				Uri.fromFile(File(this))
		}
	}

	init {
		val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
		controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
		controllerFuture.addListener({
			try {
				val controller = controllerFuture.get()
				_player.value = controller
				controller.addListener(createListener())
				syncStateWithController(controller)
			} catch (e: Exception) {
				Timber.e(e)
			}
		}, MoreExecutors.directExecutor())
	}

	private fun syncStateWithController(controller: Player) {
		if (controller.isPlaying) handler.post(progressRunnable)
	}

	private val progressRunnable = object : Runnable {
		override fun run() {
			_player.value?.let { p ->
				_state.update {
					it.copy(position = p.currentPosition, duration = p.duration)
				}
				if (p.isPlaying) handler.postDelayed(this, 500L)
			}
		}
	}

	private fun createListener(): Player.Listener {
		return object : Player.Listener {
			override fun onEvents(player: Player, events: Player.Events) {
				if (events.contains(Player.EVENT_PLAYBACK_STATE_CHANGED) ||
					events.contains(Player.EVENT_IS_PLAYING_CHANGED)
				) {

					val isPlaying = player.isPlaying
					_state.update {
						it.copy(
							isPlaying = isPlaying,
							isBuffering = player.playbackState == Player.STATE_BUFFERING
						)
					}

					if (isPlaying) {
						handler.removeCallbacks(progressRunnable)
						handler.post(progressRunnable)
					} else {
						handler.removeCallbacks(progressRunnable)
					}
				}

				if (events.contains(Player.EVENT_PLAYER_ERROR)) {
					player.playerError?.let { error ->
						_state.update {
							it.copy(
								isError = true,
								messageError = error.message ?: "Unknown Error"
							)
						}
					}
				}

				if (events.contains(Player.EVENT_MEDIA_ITEM_TRANSITION)) {
					_index.value = player.currentMediaItemIndex
				}

				if (events.contains(Player.EVENT_TRACKS_CHANGED)) {
					val tracks = player.currentTracks
					var detectedMimeType: String? = null
					for (group in tracks.groups) {
						if (group.type == C.TRACK_TYPE_VIDEO && group.isSelected) {
							detectedMimeType = group.getTrackFormat(0).sampleMimeType
							break
						}
					}
					_state.update { it.copy(mimeType = detectedMimeType) }
				}
			}

			override fun onPlayerError(error: PlaybackException) {
				_state.update {
					it.copy(
						isError = true,
						messageError = error.message ?: "Unknown Error"
					)
				}
			}
		}
	}

	private fun buildMediaItem(url: String): MediaItem {
		val uri = url.toMediaUri()
		val lastSegment = uri.lastPathSegment?.lowercase().orEmpty()
		val mimeType = when {
			lastSegment.contains(".m3u8") || lastSegment.contains(".m3u") ->
				MimeTypes.APPLICATION_M3U8

			lastSegment.contains(".mpd") ->
				MimeTypes.APPLICATION_MPD

			lastSegment.contains(".ism") ->
				MimeTypes.APPLICATION_SS

			lastSegment.contains(".mp4") || lastSegment.contains(".m4v") ->
				MimeTypes.VIDEO_MP4

			lastSegment.contains(".mkv") ->
				MimeTypes.APPLICATION_MATROSKA

			lastSegment.contains(".webm") ->
				MimeTypes.VIDEO_WEBM

			lastSegment.contains(".mov") ->
				MimeTypes.VIDEO_QUICK_TIME

			lastSegment.contains(".avi") ->
				MimeTypes.VIDEO_AVI

			lastSegment.contains(".flv") ->
				MimeTypes.VIDEO_FLV

			lastSegment.contains(".ts") ->
				MimeTypes.VIDEO_MP2T

			lastSegment.contains(".mp3") ->
				MimeTypes.AUDIO_MPEG

			lastSegment.contains(".aac") ->
				MimeTypes.AUDIO_AAC

			lastSegment.contains(".m4a") ->
				MimeTypes.AUDIO_MP4

			lastSegment.contains(".flac") ->
				MimeTypes.AUDIO_FLAC

			lastSegment.contains(".wav") ->
				MimeTypes.AUDIO_WAV

			lastSegment.contains(".ogg") ->
				MimeTypes.AUDIO_OGG

			else -> null
		}
		return MediaItem.Builder()
			.setUri(uri)
			.setMediaId(url)
			.setMediaMetadata(
				MediaMetadata.Builder()
					.setTitle("Player")
					.setMediaType(MediaMetadata.MEDIA_TYPE_MOVIE)
					.build()
			)
			.apply {
				mimeType?.let { setMimeType(it) }
			}
			.build()
	}

	fun setURLs(links: List<String>) {
		val mediaItems = links.map { buildMediaItem(it) }
		_items.value = mediaItems
		_player.value?.setMediaItems(mediaItems)
		/// update state
		_state.update {
			it.copy(
				position = 0L,
				duration = 1L,
				speed = 1f, // Default speed
				sizeSubtitle = Settings.getSubtitleSize(context),
				positionSubtitle = Settings.getSubtitlePosition(context),
				subtitleTextColor = Settings.getColor(context).toArgb(),
				scaleMode = Settings.getScaleMode(context),
				isError = false,
				messageError = ""
			)
		}
	}

	fun setPlaybackSpeed(speed: Float) {
		_player.value?.let { p ->
			p.playbackParameters = PlaybackParameters(speed)
			_state.update { it.copy(speed = speed) }
		}
	}

	fun selectItem(index: Int) {
		if (index !in _items.value.indices) return
		_isPlay.value = true
		_player.value?.let { p ->
			val preferredLang = Settings.getLanguages(context)
			p.trackSelectionParameters = p.trackSelectionParameters.buildUpon()
				.setTrackTypeDisabled(C.TRACK_TYPE_VIDEO, false)
				.setPreferredAudioLanguage(preferredLang)
				.setPreferredTextLanguage(preferredLang)
				.build()
			p.seekTo(index, 0L)
			p.prepare()
			p.play()
		}
	}

	fun selectAudioItem(index: Int) {
		if (index !in _items.value.indices) return
		_player.value?.let { p ->
			val preferredLang = Settings.getLanguages(context)
			p.trackSelectionParameters = p.trackSelectionParameters.buildUpon()
				.setPreferredAudioLanguage(preferredLang)
				.setPreferredTextLanguage(preferredLang)
				.build()
			p.seekTo(index, 0L)
			p.prepare()
			p.play()
		}
	}

	fun seekTo(progress: Double) {
		_player.value?.let { p ->
			val newPosition = (progress * p.duration).toLong()
			p.seekTo(newPosition.coerceAtLeast(0L))
		}
	}

	fun getAudioTracks(): List<String> {
		val player = _player.value ?: return emptyList()
		val tracks = mutableListOf<String>()
		val currentTracks = player.currentTracks
		for (group in currentTracks.groups) {
			if (group.type == C.TRACK_TYPE_AUDIO) {
				for (i in 0 until group.length) {
					val format = group.getTrackFormat(i)
					tracks.add(format.language ?: "Unknown")
				}
			}
		}
		return tracks
	}

	fun getSubtitleTracks(): List<String> {
		val player = _player.value ?: return emptyList()
		val tracks = mutableListOf<String>()
		val currentTracks = player.currentTracks
		for (group in currentTracks.groups) {
			if (group.type == C.TRACK_TYPE_TEXT) {
				for (i in 0 until group.length) {
					val format = group.getTrackFormat(i)
					tracks.add(format.language ?: "Unknown")
				}
			}
		}
		return tracks
	}

	fun selectAudioTrack(index: Int) {
		val player = _player.value ?: return
		val currentTracks = player.currentTracks
		var trackCount = 0
		for (group in currentTracks.groups) {
			if (group.type == C.TRACK_TYPE_AUDIO) {
				for (i in 0 until group.length) {
					if (trackCount == index) {
						val language = group.getTrackFormat(i).language
						if (language != null) {
							Settings.setLanguages(context, language)
						}
						player.trackSelectionParameters = player.trackSelectionParameters
							.buildUpon()
							.setOverrideForType(
								androidx.media3.common.TrackSelectionOverride(
									group.mediaTrackGroup,
									i
								)
							)
							.build()
						return
					}
					trackCount++
				}
			}
		}
	}

	fun selectSubtitleTrack(index: Int) {
		val player = _player.value ?: return
		val currentTracks = player.currentTracks
		var trackCount = 0
		for (group in currentTracks.groups) {
			if (group.type == C.TRACK_TYPE_TEXT) {
				for (i in 0 until group.length) {
					if (trackCount == index) {
						val language = group.getTrackFormat(i).language
						if (language != null) {
							Settings.setLanguages(context, language)
						}
						player.trackSelectionParameters = player.trackSelectionParameters
							.buildUpon()
							.setOverrideForType(
								androidx.media3.common.TrackSelectionOverride(
									group.mediaTrackGroup,
									i
								)
							)
							.build()
						return
					}
					trackCount++
				}
			}
		}
	}

	fun updateSubtitleSize(size: Float) {
		_state.update { it.copy(sizeSubtitle = size) }
		Timber.tag("size").d(size.toString())
		Settings.setSubtitleSize(context, size)
	}

	fun updateSubtitlePosition(position: Float) {
		_state.update { it.copy(positionSubtitle = position) }
		Timber.tag("position").d(position.toString())
		Settings.setSubtitlePosition(context, position)
	}

	fun aspect() {
		val newAspect = when (this.state.value.scaleMode) {
			VideoScaleMode.BEST_FIT -> VideoScaleMode.FIT_SCREEN
			VideoScaleMode.FIT_SCREEN -> VideoScaleMode.FILL
			VideoScaleMode.FILL -> VideoScaleMode.RATIO_16_9
			VideoScaleMode.RATIO_16_9 -> VideoScaleMode.RATIO_4_3
			VideoScaleMode.RATIO_4_3 -> VideoScaleMode.ORIGINAL
			VideoScaleMode.ORIGINAL -> VideoScaleMode.BEST_FIT
		}
		this._state.update {
			it.copy(
				scaleMode = newAspect
			)
		}
		Settings.setScaleMode(context, newAspect)
	}

	fun toggleVideo(enabled: Boolean) {
		_player.value?.let { p ->
			p.trackSelectionParameters = p.trackSelectionParameters
				.buildUpon()
				.setTrackTypeDisabled(C.TRACK_TYPE_VIDEO, !enabled)
				.build()
		}
	}

	fun setPlayMode(isPlay: Boolean) {
		_isPlay.value = isPlay
	}

	fun stop() {
		_isPlay.value = false
		_player.value?.let { p ->
			p.stop()
			p.clearMediaItems()
		}
	}

	fun setSleepTimer(durationMs: Long) {
		Timber.tag("START_SLEEP_TIMER").d("setSleepTimer: $durationMs")
		val command = SessionCommand("START_SLEEP_TIMER", Bundle().apply {
			putLong("DURATION_MS", durationMs)
		})
		_player.value?.let { p ->
			if (p is MediaController) {
				p.sendCustomCommand(command, Bundle.EMPTY)
			}
		}
	}

	fun setSleepTimerEndOfEpisode() {
		setSleepTimer(-1)
	}

	fun cancelSleepTimer() {
		val command = SessionCommand("CANCEL_SLEEP_TIMER", Bundle.EMPTY)
		_player.value?.let { p ->
			if (p is MediaController) {
				p.sendCustomCommand(command, Bundle.EMPTY)
			}
		}
	}

	override fun onCleared() {
		super.onCleared()
		_isPlay.value = false
		handler.removeCallbacks(progressRunnable)
		MediaController.releaseFuture(controllerFuture)
	}
}
