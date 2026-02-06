package com.nvv.mediadata.data.viewmodel

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import com.nvv.mediadata.data.model.PlaybackState
import com.nvv.mediadata.data.model.VideoScaleMode
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.anilbeesetti.nextlib.media3ext.ffdecoder.NextRenderersFactory
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


@OptIn(UnstableApi::class)
@HiltViewModel
class PlayerViewModel @Inject constructor(
	@ApplicationContext private val context: Context,
	@Named("BaseOkHttpClient")
	private val okHttpClient: OkHttpClient,
) : ViewModel() {
	private companion object {
		private const val MIME_VIDEO_VP8 = "video/x-vnd.on2.vp8"
		private const val MIME_AUDIO_ALAC = "audio/alac"
		private const val MIME_AUDIO_TRUEHD = "audio/true-hd"
		private const val MIME_AUDIO_RAW = "audio/raw"
		private const val MIME_AUDIO_MP4 = "audio/mp4"
		private const val MIME_AUDIO_AMR = "audio/amr"
		private const val MIME_AUDIO_AMR_WB = "audio/amr-wb"
	}

	private var _isPlay = MutableStateFlow(false)
	val isPlay = _isPlay.asStateFlow()

	private val _player = MutableStateFlow<Player?>(null)
	val player = _player.asStateFlow()

	private val _state = MutableStateFlow(PlaybackState())
	val state = _state.asStateFlow()

	/** Playback state (alias for UI). */
	val playbackState = _state.asStateFlow()

	private val _errorState = MutableStateFlow<String?>(null)
	val errorState = _errorState.asStateFlow()

	private val _currentPlayingUrl = MutableStateFlow<String?>(null)
	val currentPlayingUrl = _currentPlayingUrl.asStateFlow()

	private val _index = MutableStateFlow(0)
	val currentMediaIndex = _index.asStateFlow()

	private val _items = MutableStateFlow<List<MediaItem>>(emptyList())
	private var _currentUrls = emptyList<String>()

	private val handler = Handler(Looper.getMainLooper())

	private var ffmpegPlayer: ExoPlayer? = null
	private var defaultPlayer: ExoPlayer? = null

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

	private fun createPlayerConfig(): Triple<DefaultTrackSelector, DefaultLoadControl, DefaultDataSource.Factory> {
		val trackSelector = DefaultTrackSelector(context)
		val bufferConfig = BufferCalculator.calculateBufferConfig(context)
		val loadControl = DefaultLoadControl.Builder()
			.setBufferDurationsMs(
				bufferConfig.minBuffer,
				bufferConfig.maxBuffer,
				bufferConfig.bufferForPlayback,
				bufferConfig.bufferForPlaybackAfterRebuffer
			)
			.setBackBuffer(
				bufferConfig.maxBuffer.coerceAtMost(30_000),
				false
			)
			.build()
		val dataSourceFactory = DefaultDataSource.Factory(
			context, DefaultHttpDataSource.Factory()
		)
		return Triple(trackSelector, loadControl, dataSourceFactory)
	}

	private fun createExoPlayer(renderersFactory: androidx.media3.exoplayer.RenderersFactory): ExoPlayer {
		val (trackSelector, loadControl, dataSourceFactory) = createPlayerConfig()
		return ExoPlayer.Builder(context)
			.setRenderersFactory(renderersFactory)
			.setTrackSelector(trackSelector)
			.setLoadControl(loadControl)
			.setMediaSourceFactory(
				DefaultMediaSourceFactory(context)
					.setDataSourceFactory(dataSourceFactory)
			)
			.setAudioAttributes(
				AudioAttributes.Builder()
					.setUsage(C.USAGE_MEDIA)
					.setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
					.build(),
				true
			)
			.setHandleAudioBecomingNoisy(true)
			.setWakeMode(C.WAKE_MODE_NETWORK)
			.build()
	}

	private fun setupPlayerListeners(targetPlayer: ExoPlayer, isFfmpegPlayer: Boolean) {
		targetPlayer.addListener(object : Player.Listener {
			override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
				if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_PLAYLIST_CHANGED) {
					val current = _player.value
					if (current is ExoPlayer && !isFfmpegPlayer && current == defaultPlayer && targetPlayer.mediaItemCount > 0) {
						handler.postDelayed({
							if (_player.value == defaultPlayer) {
								switchToFfmpegPlayer()
							}
						}, 100)
					}
				}
				updateCurrentPlayingUrlFromPlayer(targetPlayer)
			}

			override fun onPlayerError(error: PlaybackException) {
				if (isFfmpegPlayer && _player.value == ffmpegPlayer) {
					val errorMessage = error.message ?: ""
					val cause = error.cause
					val isFfmpegError = errorMessage.contains("Ffmpeg", ignoreCase = true) ||
							errorMessage.contains("FfmpegVideoRenderer", ignoreCase = true) ||
							errorMessage.contains("FfmpegDecoderException", ignoreCase = true)
					val isOutOfMemoryError = cause is OutOfMemoryError ||
							(cause?.cause is OutOfMemoryError) ||
							errorMessage.contains("OutOfMemoryError", ignoreCase = true)
					if (isFfmpegError) {
						Timber.tag("PlayerViewModel").w("FFmpeg player error: $errorMessage")
						if (isOutOfMemoryError) {
							Timber.tag("PlayerViewModel").w("OutOfMemoryError - switching to default player")
						}
						handler.post { switchToDefaultPlayer() }
					}
				}
			}

			override fun onPlaybackStateChanged(playbackState: Int) {
				super.onPlaybackStateChanged(playbackState)
				if (playbackState == Player.STATE_ENDED) {
					_state.update { it.copy(isPlaying = false) }
				}
			}
		})
	}

	private fun updateCurrentPlayingUrlFromPlayer(p: Player) {
		val idx = p.currentMediaItemIndex
		val url = _currentUrls.getOrNull(idx) ?: p.currentMediaItem?.localConfiguration?.uri?.toString()
		_currentPlayingUrl.value = url
	}

	private fun switchToFfmpegPlayer() {
		val ff = ffmpegPlayer ?: return
		if (_player.value == ff) return
		swapPlayer(ff)
	}

	private fun switchToDefaultPlayer() {
		val def = defaultPlayer ?: return
		if (_player.value == def) return
		swapPlayer(def)
	}

	private fun swapPlayer(newPlayer: ExoPlayer) {
		val oldPlayer = _player.value as? ExoPlayer ?: return
		if (oldPlayer == newPlayer) return
		val playWhenReady = oldPlayer.playWhenReady
		val currentItemIndex = oldPlayer.currentMediaItemIndex
		val playbackPositionMs = oldPlayer.currentPosition
		val trackSelectionParams = oldPlayer.trackSelectionParameters
		val mediaItems = mutableListOf<MediaItem>()
		for (i in 0 until oldPlayer.mediaItemCount) {
			mediaItems.add(oldPlayer.getMediaItemAt(i))
		}
		oldPlayer.stop()
		oldPlayer.clearMediaItems()
		val preferredLang = Settings.getLanguages(context)
		newPlayer.trackSelectionParameters = trackSelectionParams.buildUpon()
			.setPreferredAudioLanguage(preferredLang)
			.setPreferredTextLanguage(preferredLang)
			.build()
		_player.value = newPlayer
		if (mediaItems.isNotEmpty()) {
			val currentIndex = currentItemIndex.coerceIn(0, mediaItems.size - 1)
			newPlayer.setMediaItems(mediaItems, currentIndex, playbackPositionMs)
			newPlayer.prepare()
			newPlayer.playWhenReady = playWhenReady
		}
		updateCurrentPlayingUrlFromPlayer(newPlayer)
	}

	init {
		val priorityRenderersFactory = PriorityRenderersFactory(context)
			.setEnableDecoderFallback(true)
			.setExtensionRendererMode(EXTENSION_RENDERER_MODE_PREFER)
		val nextRenderersFactory = NextRenderersFactory(context)
			.setEnableDecoderFallback(true)
			.setExtensionRendererMode(EXTENSION_RENDERER_MODE_PREFER)
		val ff = createExoPlayer(priorityRenderersFactory)
		val def = createExoPlayer(nextRenderersFactory)
		setupPlayerListeners(ff, true)
		setupPlayerListeners(def, false)
		val preferredLang = Settings.getLanguages(context)
		ff.trackSelectionParameters = ff.trackSelectionParameters.buildUpon()
			.setPreferredAudioLanguage(preferredLang)
			.setPreferredTextLanguage(preferredLang)
			.build()
		def.trackSelectionParameters = def.trackSelectionParameters.buildUpon()
			.setPreferredAudioLanguage(preferredLang)
			.setPreferredTextLanguage(preferredLang)
			.build()
		ffmpegPlayer = ff
		defaultPlayer = def
		_player.value = ff
		ff.addListener(createListener())
		def.addListener(createListener())
		handler.post { syncStateWithPlayer(ff) }
	}

	private fun syncStateWithPlayer(p: Player) {
		if (p.isPlaying) handler.post(progressRunnable)
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
						val detailedMessage = buildDetailedErrorMessage(player, error)
						_state.update {
							it.copy(isError = true, messageError = detailedMessage)
						}
						_errorState.value = detailedMessage
					}
				}

				if (events.contains(Player.EVENT_MEDIA_ITEM_TRANSITION)) {
					_index.value = player.currentMediaItemIndex
					updateCurrentPlayingUrlFromPlayer(player)
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
				val p = _player.value
				val detailedMessage = if (p != null) buildDetailedErrorMessage(p, error) else (error.message ?: "Unknown Error")
				_state.update {
					it.copy(isError = true, messageError = detailedMessage)
				}
				_errorState.value = detailedMessage
			}

			override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
				super.onMediaMetadataChanged(mediaMetadata)
				val title = mediaMetadata.title ?: mediaMetadata.displayTitle
				if (title != null) {
					Timber.d("Detected Title from Stream: $title")
					val current = _player.value ?: return
					val currentIndex = current.currentMediaItemIndex
					val currentItem = current.getMediaItemAt(currentIndex)
					val newMetadata = currentItem.mediaMetadata.buildUpon()
						.setTitle(title)
						.setDisplayTitle(title)
						.build()
					val newItem = currentItem.buildUpon()
						.setMediaMetadata(newMetadata)
						.build()
					current.replaceMediaItem(currentIndex, newItem)
				}
			}
		}
	}

	private fun buildDetailedErrorMessage(player: Player, error: PlaybackException): String {
		val errorCode = error.errorCode
		val baseMessage = error.message ?: "Unknown Error"
		if (errorCode != PlaybackException.ERROR_CODE_DECODER_INIT_FAILED &&
			errorCode != PlaybackException.ERROR_CODE_DECODER_QUERY_FAILED
		) {
			return baseMessage
		}
		val tracks = player.currentTracks
		var videoMimeType: String? = null
		var videoCodecs: String? = null
		for (group in tracks.groups) {
			if (group.type == C.TRACK_TYPE_VIDEO && group.length > 0) {
				val format = group.getTrackFormat(0)
				videoMimeType = format.sampleMimeType
				videoCodecs = format.codecs
				break
			}
		}
		if (videoMimeType == null) return baseMessage
		val supportResult = CodecSupportChecker.checkVideoFormatSupport(videoMimeType, videoCodecs)
		return buildString {
			append("Can't play video\n\n")
			append("Format: $videoMimeType\n")
			if (videoCodecs != null) append("Codec: $videoCodecs\n")
			append("Error: $baseMessage\n\n")
			if (!supportResult.isSupported && supportResult.errorMessage != null) {
				append(supportResult.errorMessage)
			} else if (supportResult.availableDecoders.isEmpty()) {
				append("Can't found decoder suitable in this device.\n")
			}
		}
	}

	private suspend fun getMimeTypeFromServer(url: String): String? = withContext(Dispatchers.IO) {
		try {
			val request = Request.Builder()
				.url(url)
				.get()
				.header("Range", "bytes=0-4095")
				.build()
			okHttpClient.newCall(request).execute().use { response ->
				val rawContentType = response.header("Content-Type")?.lowercase()
				val contentType = rawContentType
					?.substringBefore(";")
					?.trim()
					?.takeIf { it.isNotBlank() }
					?: ""
				if (contentType == "" ||
					contentType == "application/octet-stream" ||
					contentType == "binary/octet-stream" ||
					contentType == "application/force-download"
				) {
					return@withContext sniffMimeTypeFromBytes(url)
				}
				return@withContext when {
					contentType.contains("video/vp8") || contentType.contains("x-vnd.on2.vp8") ->
						MIME_VIDEO_VP8

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

					contentType.contains("truehd") || contentType.contains("true-hd") ->
						MIME_AUDIO_TRUEHD

					contentType.contains("alac") ->
						MIME_AUDIO_ALAC

					contentType.contains("vnd.dts.hd") ||
							contentType.contains("dts-hd") ||
							contentType.contains("dts_hd") ->
						MimeTypes.AUDIO_DTS_HD

					contentType.contains("audio/raw") ||
							contentType.contains("lpcm") ||
							contentType.contains("pcm") ||
							contentType.contains("audio/l16") ||
							contentType.contains("audio/l24") ->
						MIME_AUDIO_RAW

					contentType.contains("amr-wb") ->
						MIME_AUDIO_AMR_WB

					contentType.contains("audio/amr") ||
							contentType.contains("amr") ->
						MIME_AUDIO_AMR

					contentType.contains("audio/mpeg") ||
							contentType.contains("mp3") ->
						MimeTypes.AUDIO_MPEG

					contentType.contains("audio/aac") ||
							contentType.contains("mp4a") ->
						MimeTypes.AUDIO_AAC

					contentType.contains("audio/eac3-joc") ||
							(contentType.contains("eac3") && contentType.contains("joc")) ->
						MimeTypes.AUDIO_E_AC3_JOC

					contentType.contains("audio/opus") ||
							contentType.contains("opus") ->
						MimeTypes.AUDIO_OPUS

					contentType.contains("vorbis") ->
						MimeTypes.AUDIO_VORBIS

					contentType.contains("audio/ogg") ->
						MimeTypes.AUDIO_OGG

					contentType.contains("audio/wav") ||
							contentType.contains("wave") ->
						MimeTypes.AUDIO_WAV

					contentType.contains("audio/flac") ||
							contentType.contains("x-flac") ->
						MimeTypes.AUDIO_FLAC

					contentType.contains("audio/ac3") ->
						MimeTypes.AUDIO_AC3

					contentType.contains("audio/eac3") ->
						MimeTypes.AUDIO_E_AC3

					contentType.contains("audio/x-dts") ||
							contentType.contains("dts") ->
						MimeTypes.AUDIO_DTS

					contentType.contains("text/vtt") -> MimeTypes.TEXT_VTT
					contentType.contains("application/x-subrip") -> MimeTypes.APPLICATION_SUBRIP
					contentType.contains("application/ttml+xml") -> MimeTypes.APPLICATION_TTML
					else -> null
				}
			}
		} catch (e: Exception) {
			null
		}
	}

	private suspend fun sniffMimeTypeFromBytes(url: String): String? = withContext(Dispatchers.IO) {
		return@withContext try {
			val request = Request.Builder()
				.url(url)
				.get()
				.header("Range", "bytes=0-4095")
				.build()
			okHttpClient.newCall(request).execute().use { response ->
				val bytes = response.body?.bytes() ?: return@withContext null
				if (bytes.isEmpty()) return@withContext null
				val headText = bytes.toString(Charsets.UTF_8).trimStart()
				if (headText.startsWith("#EXTM3U", ignoreCase = true)) return@withContext MimeTypes.APPLICATION_M3U8
				if (headText.startsWith("<MPD", ignoreCase = true) || headText.contains("<MPD", ignoreCase = true)) {
					return@withContext MimeTypes.APPLICATION_MPD
				}
				if (bytes.size >= 4 &&
					bytes[0] == 0x1A.toByte() &&
					bytes[1] == 0x45.toByte() &&
					bytes[2] == 0xDF.toByte() &&
					bytes[3] == 0xA3.toByte()
				) return@withContext MimeTypes.APPLICATION_MATROSKA
				if (bytes.size >= 12 &&
					bytes[4] == 'f'.code.toByte() &&
					bytes[5] == 't'.code.toByte() &&
					bytes[6] == 'y'.code.toByte() &&
					bytes[7] == 'p'.code.toByte()
				) return@withContext MimeTypes.VIDEO_MP4
				if (bytes[0] == 0x47.toByte()) return@withContext MimeTypes.VIDEO_MP2T
				if (bytes.size >= 4 &&
					bytes[0] == 'O'.code.toByte() &&
					bytes[1] == 'g'.code.toByte() &&
					bytes[2] == 'g'.code.toByte() &&
					bytes[3] == 'S'.code.toByte()
				) return@withContext MimeTypes.AUDIO_OGG
				if (bytes.size >= 4 &&
					bytes[0] == 'f'.code.toByte() &&
					bytes[1] == 'L'.code.toByte() &&
					bytes[2] == 'a'.code.toByte() &&
					bytes[3] == 'C'.code.toByte()
				) return@withContext MimeTypes.AUDIO_FLAC
				if (bytes.size >= 12 &&
					bytes[0] == 'R'.code.toByte() &&
					bytes[1] == 'I'.code.toByte() &&
					bytes[2] == 'F'.code.toByte() &&
					bytes[3] == 'F'.code.toByte() &&
					bytes[8] == 'W'.code.toByte() &&
					bytes[9] == 'A'.code.toByte() &&
					bytes[10] == 'V'.code.toByte() &&
					bytes[11] == 'E'.code.toByte()
				) return@withContext MimeTypes.AUDIO_WAV
				if (bytes.size >= 3 &&
					bytes[0] == 'I'.code.toByte() &&
					bytes[1] == 'D'.code.toByte() &&
					bytes[2] == '3'.code.toByte()
				) return@withContext MimeTypes.AUDIO_MPEG
				if (bytes.size >= 2 && bytes[0] == 0xFF.toByte() && (bytes[1].toInt() and 0xE0) == 0xE0) {
					return@withContext MimeTypes.AUDIO_MPEG
				}

				null
			}
		} catch (e: Exception) {
			null
		}
	}

	private suspend fun buildMediaItem(url: String): MediaItem {
		val uri = url.toMediaUri()
		val path = uri.toString().lowercase()
		val mimeType = when {
			path.contains(".m3u8") -> MimeTypes.APPLICATION_M3U8
			path.contains(".mpd") -> MimeTypes.APPLICATION_MPD
			path.endsWith(".m4a") -> MIME_AUDIO_MP4
			path.endsWith(".alac") -> MIME_AUDIO_ALAC
			path.endsWith(".truehd") -> MIME_AUDIO_TRUEHD
			path.endsWith(".awb") -> MIME_AUDIO_AMR_WB
			path.endsWith(".amr") -> MIME_AUDIO_AMR
			path.endsWith(".flac") -> MimeTypes.AUDIO_FLAC
			path.endsWith(".wav") ||
					path.endsWith(".wave") -> MimeTypes.AUDIO_WAV

			path.endsWith(".mp3") -> MimeTypes.AUDIO_MPEG
			path.endsWith(".aac") -> MimeTypes.AUDIO_AAC
			path.endsWith(".opus") -> MimeTypes.AUDIO_OPUS
			path.endsWith(".ogg") -> MimeTypes.AUDIO_OGG
			path.endsWith(".ac3") -> MimeTypes.AUDIO_AC3
			path.endsWith(".eac3") -> MimeTypes.AUDIO_E_AC3
			path.endsWith(".dts") ||
					path.endsWith(".dtshd") ||
					path.endsWith(".dts-hd") -> MimeTypes.AUDIO_DTS

			path.contains(".hevc") ||
					path.contains(".h265") -> MimeTypes.VIDEO_H265

			path.contains(".av1") -> MimeTypes.VIDEO_AV1
			path.contains(".h264") || path.contains(".avc") -> MimeTypes.VIDEO_H264
			path.endsWith(".avi") -> "video/x-msvideo"
			path.endsWith(".mkv") || path.contains("matroska") -> MimeTypes.APPLICATION_MATROSKA
			path.endsWith(".mka") -> MimeTypes.APPLICATION_MATROSKA
			path.endsWith(".webm") -> MimeTypes.VIDEO_WEBM
			path.endsWith(".flv") -> MimeTypes.VIDEO_FLV
			path.endsWith(".mp4") || path.endsWith(".m4v") -> MimeTypes.VIDEO_MP4
			else -> getMimeTypeFromServer(url)
		}
		val metadata = MediaMetadata.Builder()
			.setMediaType(MediaMetadata.MEDIA_TYPE_MOVIE)
			.build()
		val builder = MediaItem.Builder()
			.setUri(uri)
			.setMediaMetadata(metadata)
		if (!mimeType.isNullOrBlank()) {
			builder.setMimeType(mimeType)
		}
		return builder.build()
	}

	suspend fun setURLs(links: List<String>) {
		val mediaItems = links.map { buildMediaItem(it) }
		_items.value = mediaItems
		_currentUrls = links
		_currentPlayingUrl.value = links.firstOrNull()
		_errorState.value = null
		_player.value?.setMediaItems(mediaItems)
		_state.update {
			it.copy(
				position = 0L,
				duration = 1L,
				speed = 1f,
				sizeSubtitle = Settings.getSubtitleSize(context),
				positionSubtitle = Settings.getSubtitlePosition(context),
				subtitleTextColor = Settings.getColor(context).toArgb(),
				subtitleOutlineColor = Settings.getSubtitleOutlineColor(context),
				subtitleBackgroundColor = Settings.getSubtitleBackgroundColor(context),
				scaleMode = Settings.getScaleMode(context),
				subtitleFont = Settings.getSubtitleFont(context),
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

	fun updateSubtitleSettings() {
		_state.update {
			it.copy(
				subtitleTextColor = Settings.getColor(context).toArgb(),
				subtitleOutlineColor = Settings.getSubtitleOutlineColor(context),
				subtitleBackgroundColor = Settings.getSubtitleBackgroundColor(context),
				subtitleFont = Settings.getSubtitleFont(context),
				sizeSubtitle = Settings.getSubtitleSize(context),
				positionSubtitle = Settings.getSubtitlePosition(context)
			)
		}
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
		_currentPlayingUrl.value = null
		_currentUrls = emptyList()
		_errorState.value = null
		_player.value?.let { p ->
			p.stop()
			p.clearMediaItems()
		}
	}

	override fun onCleared() {
		super.onCleared()
		_isPlay.value = false
		_currentPlayingUrl.value = null
		_currentUrls = emptyList()
		_errorState.value = null
		handler.removeCallbacks(progressRunnable)
		_player.value = null
		ffmpegPlayer?.release()
		ffmpegPlayer = null
		defaultPlayer?.release()
		defaultPlayer = null
	}
}
