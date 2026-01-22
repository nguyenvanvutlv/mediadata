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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Named
import kotlin.text.contains

@OptIn(UnstableApi::class)
@HiltViewModel
class PlayerViewModel @Inject constructor(
	@ApplicationContext private val context: Context,
	@Named("BaseOkHttpClient")
	private val okHttpClient: OkHttpClient,
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
		val sessionToken = SessionToken(context.applicationContext,
			ComponentName(context.applicationContext, PlaybackService::class.java))
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

			override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
				super.onMediaMetadataChanged(mediaMetadata)
				val title = mediaMetadata.title ?: mediaMetadata.displayTitle
				if (title != null) {
					Timber.d("Detected Title from Stream: $title")
					val p = _player.value ?: return
					val currentIndex = p.currentMediaItemIndex
					val currentItem = p.getMediaItemAt(currentIndex)
					val newMetadata = currentItem.mediaMetadata.buildUpon()
						.setTitle(title)
						.setDisplayTitle(title)
						.build()
					val newItem = currentItem.buildUpon()
						.setMediaMetadata(newMetadata)
						.build()
					p.replaceMediaItem(currentIndex, newItem)
				}
			}
		}
	}

	private suspend fun getMimeTypeFromServer(url: String): String? = withContext(Dispatchers.IO) {
		try {
			val request = Request.Builder()
				.url(url)
				.head()
				.build()
			okHttpClient.newCall(request).execute().use { response ->
				val contentType = response.header("Content-Type")?.lowercase()
				Timber.tag("mimetype").d("Raw Content-Type: $contentType | URL: $url")
				if (contentType == null) return@withContext null
				return@withContext when {
					contentType.contains("mpegurl") || contentType.contains("m3u8") ->
						MimeTypes.APPLICATION_M3U8
					contentType.contains("dash+xml") || contentType.contains("dash") ->
						MimeTypes.APPLICATION_MPD
					contentType.contains("vnd.ms-sstr+xml") ->
						MimeTypes.APPLICATION_SS
					contentType.contains("video/x-matroska") || contentType.contains("mkv") ->
						MimeTypes.APPLICATION_MATROSKA
					contentType.contains("video/mp4") || contentType.contains("m4v") ->
						MimeTypes.VIDEO_MP4
					contentType.contains("video/webm") ->
						MimeTypes.VIDEO_WEBM
					contentType.contains("video/x-msvideo") || contentType.contains("avi") ->
						"video/x-msvideo" // AVI
					contentType.contains("video/quicktime") ->
						MimeTypes.VIDEO_QUICK_TIME
					contentType.contains("video/x-flv") || contentType.contains("flv") ->
						MimeTypes.VIDEO_FLV
					contentType.contains("video/mp2t") || contentType.contains("ts") ->
						MimeTypes.VIDEO_MP2T
					contentType.contains("audio/mpeg") || contentType.contains("mp3") ->
						MimeTypes.AUDIO_MPEG
					contentType.contains("audio/aac") || contentType.contains("mp4a") ->
						MimeTypes.AUDIO_AAC
					contentType.contains("audio/ogg") || contentType.contains("opus") ->
						MimeTypes.AUDIO_OGG
					contentType.contains("audio/wav") || contentType.contains("wave") ->
						MimeTypes.AUDIO_WAV
					contentType.contains("audio/flac") || contentType.contains("x-flac") ->
						MimeTypes.AUDIO_FLAC
					contentType.contains("audio/ac3") ->
						MimeTypes.AUDIO_AC3
					contentType.contains("audio/eac3") ->
						MimeTypes.AUDIO_E_AC3
					contentType.contains("audio/x-dts") || contentType.contains("dts") ->
						MimeTypes.AUDIO_DTS
					contentType.contains("text/vtt") -> MimeTypes.TEXT_VTT
					contentType.contains("application/x-subrip") -> MimeTypes.APPLICATION_SUBRIP
					contentType.contains("application/ttml+xml") -> MimeTypes.APPLICATION_TTML
					else -> contentType
				}
			}
		} catch (e: Exception) {
			Timber.tag("mimetype").e(e, "Error sniffing MimeType for $url")
			null
		}
	}

	private suspend fun buildMediaItem(url: String): MediaItem {
		val uri = url.toMediaUri()
		val path = uri.toString().lowercase()
		val mimeType = when {
			path.contains(".m3u8") -> MimeTypes.APPLICATION_M3U8
			path.contains(".mpd") -> MimeTypes.APPLICATION_MPD
			path.contains(".hevc") || path.contains(".h265") -> MimeTypes.VIDEO_H265
			path.contains(".av1") -> MimeTypes.VIDEO_AV1
			path.contains(".h264") || path.contains(".avc") -> MimeTypes.VIDEO_H264
			path.endsWith(".avi") -> "video/x-msvideo"
			path.endsWith(".mkv") || path.contains("matroska") -> MimeTypes.APPLICATION_MATROSKA
			path.endsWith(".webm") -> MimeTypes.VIDEO_WEBM
			path.endsWith(".flv") -> MimeTypes.VIDEO_FLV
			path.endsWith(".mp4") || path.endsWith(".m4v") ||
					path.startsWith("http") -> MimeTypes.VIDEO_MP4
			else -> getMimeTypeFromServer(url)
		}
		Timber.tag("mimetype").d("MediaItem MimeType: $mimeType")
		val metadata = MediaMetadata.Builder()
			.setMediaType(MediaMetadata.MEDIA_TYPE_MOVIE)
			.build()
		return MediaItem.Builder()
			.setUri(uri)
			.setMimeType(mimeType)
			.setMediaMetadata(metadata)
			.build()
	}

	suspend fun setURLs(links: List<String>) {
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



	override fun onCleared() {
		super.onCleared()
		_isPlay.value = false
		handler.removeCallbacks(progressRunnable)
		MediaController.releaseFuture(controllerFuture)
	}
}
