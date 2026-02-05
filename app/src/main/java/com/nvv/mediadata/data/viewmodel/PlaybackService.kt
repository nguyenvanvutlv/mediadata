package com.nvv.mediadata.data.viewmodel

import android.app.ActivityManager
import android.app.PendingIntent
import android.app.UiModeManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.annotation.OptIn
import androidx.media3.cast.CastPlayer
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.SessionManagerListener
import com.nvv.mediadata.app.MainActivity
import io.github.anilbeesetti.nextlib.media3ext.ffdecoder.NextRenderersFactory
import timber.log.Timber


const val DEFAULT_BUFFER_FOR_PLAYBACK = 2_500
const val DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER = 3_000
const val DEFAULT_MAX_BUFFER = 30_000
const val DEFAULT_MIN_BUFFER = 6_000

object BufferCalculator {
	fun calculateBufferConfig(context: Context): BufferConfig {
		val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
		val memoryInfo = ActivityManager.MemoryInfo()
		activityManager?.getMemoryInfo(memoryInfo)
		val availableRamMB = memoryInfo.availMem / (1024 * 1024)
		val totalRamMB = memoryInfo.totalMem / (1024 * 1024)
		val isLowMemoryDevice = memoryInfo.lowMemory
		val androidVersionFactor = when {
			Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> 1.2f
			else -> 0.9f
		}
		val ramMultiplier = when {
			isLowMemoryDevice || availableRamMB < 1_000 -> 0.4f
			availableRamMB < 2_000 -> 0.6f
			availableRamMB < 3_000 -> 0.8f
			availableRamMB < 4_000 -> 1.0f
			availableRamMB < 6_000 -> 1.2f
			else -> 1.5f
		}
		val finalMultiplier = (ramMultiplier * androidVersionFactor).coerceIn(0.3f, 1.8f)
		val minBufferLimit = if (isLowMemoryDevice || availableRamMB < 2_000) {
			Pair(2_000, 6_000)
		} else {
			Pair(3_000, 10_000)
		}
		val maxBufferLimit = if (isLowMemoryDevice || availableRamMB < 2_000) {
			Pair(12_000, 25_000)
		} else {
			Pair(15_000, 60_000)
		}
		val minBuffer = (DEFAULT_MIN_BUFFER * finalMultiplier).toInt().coerceIn(minBufferLimit.first, minBufferLimit.second)
		val maxBuffer = (DEFAULT_MAX_BUFFER * finalMultiplier).toInt().coerceIn(maxBufferLimit.first, maxBufferLimit.second)
		val bufferForPlayback = (DEFAULT_BUFFER_FOR_PLAYBACK * finalMultiplier).toInt().coerceIn(1_000, 5_000)
		val bufferForPlaybackAfterRebuffer = (DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER * finalMultiplier).toInt().coerceIn(1_500, 6_000)
		val safeMinBuffer = minBuffer.coerceAtMost(maxBuffer - 2_000)
		Timber.tag("BufferCalculator").d("Device RAM: ${totalRamMB}MB total, ${availableRamMB}MB available")
		Timber.tag("BufferCalculator").d("Low memory device: $isLowMemoryDevice")
		Timber.tag("BufferCalculator").d("Android version: ${Build.VERSION.SDK_INT}, factor: $androidVersionFactor")
		Timber.tag("BufferCalculator").d("RAM multiplier: $ramMultiplier, final multiplier: $finalMultiplier")
		Timber.tag("BufferCalculator").d("Calculated buffers - min: ${safeMinBuffer}ms, max: ${maxBuffer}ms, playback: ${bufferForPlayback}ms, afterRebuffer: ${bufferForPlaybackAfterRebuffer}ms")
		return BufferConfig(
			minBuffer = safeMinBuffer,
			maxBuffer = maxBuffer,
			bufferForPlayback = bufferForPlayback,
			bufferForPlaybackAfterRebuffer = bufferForPlaybackAfterRebuffer
		)
	}

	data class BufferConfig(
		val minBuffer: Int,
		val maxBuffer: Int,
		val bufferForPlayback: Int,
		val bufferForPlaybackAfterRebuffer: Int
	)
}

@OptIn(UnstableApi::class)
class PlaybackService : MediaSessionService() {
	private companion object {
		private const val MIME_VIDEO_VP8 = "video/x-vnd.on2.vp8"
		private const val MIME_AUDIO_ALAC = "audio/alac"
		private const val MIME_AUDIO_TRUEHD = "audio/true-hd"
		private const val MIME_AUDIO_RAW = "audio/raw"
		private const val MIME_AUDIO_AMR = "audio/amr"
		private const val MIME_AUDIO_AMR_WB = "audio/amr-wb"
	}

	private var mediaSession: MediaSession? = null
	private lateinit var ffmpegPlayer: ExoPlayer
	private lateinit var defaultPlayer: ExoPlayer
	lateinit var player: ExoPlayer
	private lateinit var castPlayer: CastPlayer
	private lateinit var castContext: CastContext
	private var sessionManagerListener: SessionManagerListener<CastSession>? = null

	private fun createPlayerConfig(): Triple<DefaultTrackSelector, DefaultLoadControl, DefaultDataSource.Factory> {
		val trackSelector = DefaultTrackSelector(this)
		val bufferConfig = BufferCalculator.calculateBufferConfig(this)
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
			this, DefaultHttpDataSource.Factory()
		)
		return Triple(trackSelector, loadControl, dataSourceFactory)
	}

	private fun createExoPlayer(renderersFactory: androidx.media3.exoplayer.RenderersFactory): ExoPlayer {
		val (trackSelector, loadControl, dataSourceFactory) = createPlayerConfig()
		return ExoPlayer.Builder(this)
			.setRenderersFactory(renderersFactory)
			.setTrackSelector(trackSelector)
			.setLoadControl(loadControl)
			.setMediaSourceFactory(
				DefaultMediaSourceFactory(this)
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
					if (!isFfmpegPlayer && player == defaultPlayer && targetPlayer.mediaItemCount > 0) {
						Handler(Looper.getMainLooper()).postDelayed({
							if (player == defaultPlayer) {
								switchToFfmpegPlayer()
							}
						}, 100)
					}
				}
			}

			override fun onPlayerError(error: PlaybackException) {
				if (isFfmpegPlayer && player == ffmpegPlayer) {
					val errorMessage = error.message ?: ""
					val cause = error.cause
					val isFfmpegError = errorMessage.contains("Ffmpeg", ignoreCase = true) ||
							errorMessage.contains("FfmpegVideoRenderer", ignoreCase = true) ||
							errorMessage.contains("FfmpegDecoderException", ignoreCase = true)
					val isOutOfMemoryError = cause is OutOfMemoryError ||
							(cause?.cause is OutOfMemoryError) ||
							errorMessage.contains("OutOfMemoryError", ignoreCase = true)
					if (isFfmpegError) {
						Timber.tag("PlaybackService").w("FFmpeg player error: $errorMessage")
						if (isOutOfMemoryError) {
							Timber.tag("PlaybackService").w("OutOfMemoryError - switching to default player")
						}
						Handler(Looper.getMainLooper()).post {
							switchToDefaultPlayer()
						}
					}
				}
			}

			override fun onPlaybackStateChanged(playbackState: Int) {
				super.onPlaybackStateChanged(playbackState)
				// When playback fully finishes, stop the service / notification.
				if (playbackState == Player.STATE_ENDED) {
					handlePlaybackEnded()
				}
			}
		})
	}

	private fun handlePlaybackEnded() {
		try {
			stopForeground(STOP_FOREGROUND_REMOVE)
		} catch (_: Exception) {
			// Ignore and still stop the service.
		} finally {
			stopSelf()
		}
	}

	private fun initializePlayers() {
		val priorityRenderersFactory = PriorityRenderersFactory(this)
			.setEnableDecoderFallback(true)
			.setExtensionRendererMode(EXTENSION_RENDERER_MODE_PREFER)
		val nextRenderersFactory = NextRenderersFactory(this)
			.setEnableDecoderFallback(true)
			.setExtensionRendererMode(EXTENSION_RENDERER_MODE_PREFER)
		ffmpegPlayer = createExoPlayer(priorityRenderersFactory)
		defaultPlayer = createExoPlayer(nextRenderersFactory)
		setupPlayerListeners(ffmpegPlayer, true)
		setupPlayerListeners(defaultPlayer, false)
		val preferredLang = Settings.getLanguages(this)
		ffmpegPlayer.trackSelectionParameters = ffmpegPlayer.trackSelectionParameters.buildUpon()
			.setPreferredAudioLanguage(preferredLang)
			.setPreferredTextLanguage(preferredLang)
			.build()
		defaultPlayer.trackSelectionParameters = defaultPlayer.trackSelectionParameters.buildUpon()
			.setPreferredAudioLanguage(preferredLang)
			.setPreferredTextLanguage(preferredLang)
			.build()
		player = ffmpegPlayer
		val intent = Intent(this, MainActivity::class.java).apply {
			flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
			putExtra("open_from_media_notification", true)
		}
		val pendingIntent = PendingIntent.getActivity(
			this,
			0,
			intent,
			PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
		)
		mediaSession = MediaSession.Builder(this, player)
			.setSessionActivity(pendingIntent)
			.build()
	}

	private fun switchToFfmpegPlayer() {
		if (player == ffmpegPlayer) return
		swapPlayer(ffmpegPlayer)
	}

	private fun switchToDefaultPlayer() {
		if (player == defaultPlayer) return
		swapPlayer(defaultPlayer)
	}

	private fun swapPlayer(newPlayer: ExoPlayer) {
		val session = mediaSession ?: return
		val oldPlayer = session.player as? ExoPlayer ?: return
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

		val preferredLang = Settings.getLanguages(this)
		newPlayer.trackSelectionParameters = trackSelectionParams.buildUpon()
			.setPreferredAudioLanguage(preferredLang)
			.setPreferredTextLanguage(preferredLang)
			.build()

		session.player = newPlayer
		player = newPlayer

		if (mediaItems.isNotEmpty()) {
			val currentIndex = currentItemIndex.coerceIn(0, mediaItems.size - 1)
			newPlayer.setMediaItems(mediaItems, currentIndex, playbackPositionMs)
			newPlayer.prepare()
			newPlayer.playWhenReady = playWhenReady
		}
	}

	override fun onCreate() {
		super.onCreate()
		initializePlayers()
		// check is tv
		val isTV = (getSystemService(UI_MODE_SERVICE) as? UiModeManager)
			?.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION
		if (isTV) return
		try {
			castContext = CastContext.getSharedInstance(this)
			castPlayer = CastPlayer.Builder(this).build()
			castPlayer.addListener(object : Player.Listener {
				override fun onTracksChanged(tracks: Tracks) {
					val groups = tracks.groups
					var isSupportedAudioSelected = false
					var fallbackAudioGroup: Tracks.Group? = null
					var fallbackAudioIndex = -1
					var isTextSelected = false
					var firstTextGroup: Tracks.Group? = null
					var firstTextIndex = -1
					for (group in groups) {
						if (group.type == C.TRACK_TYPE_AUDIO) {
							for (i in 0 until group.length) {
								val isSelected = group.isTrackSelected(i)
								val mime = group.getTrackFormat(i).sampleMimeType
								val isSupported = mime == MimeTypes.AUDIO_AAC ||
										mime == MimeTypes.AUDIO_MPEG ||
										mime == MimeTypes.AUDIO_OPUS ||
										mime == MimeTypes.AUDIO_VORBIS ||
										mime == MimeTypes.AUDIO_AC3 ||
										mime == MimeTypes.AUDIO_AC4 ||
										mime == MimeTypes.AUDIO_E_AC3 ||
										mime == MimeTypes.AUDIO_E_AC3_JOC ||
										mime == MimeTypes.AUDIO_FLAC ||
										mime == MimeTypes.AUDIO_DTS ||
										mime == MimeTypes.AUDIO_DTS_EXPRESS ||
										mime == MimeTypes.AUDIO_DTS_HD
								if (isSelected && isSupported) {
									isSupportedAudioSelected = true
								}
								if (fallbackAudioGroup == null && isSupported) {
									fallbackAudioGroup = group
									fallbackAudioIndex = i
								}
							}
						} else if (group.type == C.TRACK_TYPE_TEXT) {
							for (i in 0 until group.length) {
								if (group.isTrackSelected(i)) {
									isTextSelected = true
								}
								if (firstTextGroup == null) {
									firstTextGroup = group
									firstTextIndex = i
								}
							}
						}
					}
					val parametersBuilder = castPlayer.trackSelectionParameters.buildUpon()
					var needUpdate = false
					if (!isSupportedAudioSelected && fallbackAudioGroup != null) {
						parametersBuilder.setOverrideForType(
							TrackSelectionOverride(
								fallbackAudioGroup.mediaTrackGroup,
								fallbackAudioIndex
							)
						)
						needUpdate = true
					}
					if (!isTextSelected && firstTextGroup != null &&
						!castPlayer
							.trackSelectionParameters
							.disabledTrackTypes
							.contains(C.TRACK_TYPE_TEXT)
					) {
						parametersBuilder.setOverrideForType(
							TrackSelectionOverride(
								firstTextGroup.mediaTrackGroup,
								firstTextIndex
							)
						)
						needUpdate = true
					}
					if (needUpdate) {
						castPlayer.trackSelectionParameters = parametersBuilder.build()
					}
				}
			})
			setupCastListener(castContext)
		} catch (e: Exception) {
			Timber.e(e)
		}
	}

	private fun setupCastListener(castContext: CastContext) {
		sessionManagerListener = object : SessionManagerListener<CastSession> {
			override fun onSessionStarted(session: CastSession, sessionId: String) {
				swapPlayerToCast(castPlayer)
			}

			override fun onSessionEnded(session: CastSession, error: Int) {
				swapPlayerToCast(player)
			}

			override fun onSessionResumed(session: CastSession, wasSuspended: Boolean) {
				swapPlayerToCast(castPlayer)
			}

			override fun onSessionStarting(session: CastSession) {}
			override fun onSessionStartFailed(session: CastSession, error: Int) {}
			override fun onSessionEnding(session: CastSession) {}
			override fun onSessionResumeFailed(session: CastSession, error: Int) {}
			override fun onSessionResuming(session: CastSession, sessionId: String) {}
			override fun onSessionSuspended(session: CastSession, reason: Int) {}
		}
		castContext.sessionManager.addSessionManagerListener(
			sessionManagerListener!!,
			CastSession::class.java
		)
	}

	private fun swapPlayerToCast(newPlayer: Player) {
		val session = mediaSession ?: return
		val oldPlayer = session.player
		if (oldPlayer == newPlayer) return
		val playWhenReady = oldPlayer.playWhenReady
		val currentItemIndex = oldPlayer.currentMediaItemIndex
		val playbackPositionMs = oldPlayer.currentPosition
		val mediaItems = mutableListOf<MediaItem>()
		for (i in 0 until oldPlayer.mediaItemCount) {
			val oldItem = oldPlayer.getMediaItemAt(i)
			val builder = oldItem.buildUpon()
			if (i == currentItemIndex) {
				val tracks = oldPlayer.currentTracks
				var videoMime: String? = null
				var audioMime: String? = null
				var videoCodecs: String? = null
				for (group in tracks.groups) {
					if (group.isSelected) {
						val format = group.getTrackFormat(0)
						val detectedMime = format.containerMimeType ?: format.sampleMimeType
						if (group.type == C.TRACK_TYPE_VIDEO) {
							videoMime = detectedMime
							videoCodecs = format.codecs
						} else if (group.type == C.TRACK_TYPE_AUDIO) {
							audioMime = detectedMime
						}
					}
				}
				val originalMime = oldItem.localConfiguration?.mimeType
				val metadataCodecs = oldItem.mediaMetadata.extras?.getString("codecs") ?: ""
				val codecs = videoCodecs ?: metadataCodecs
				val isDolbyVision = codecs.contains("dvhe") || codecs.contains("dvh1") ||
						videoMime?.contains("dolby-vision") == true ||
						originalMime == MimeTypes.VIDEO_DOLBY_VISION
				val finalMime = when {
					isDolbyVision -> MimeTypes.VIDEO_DOLBY_VISION
					videoMime?.contains("av1") == true -> MimeTypes.VIDEO_AV1
					videoMime?.contains("hevc") == true || videoMime?.contains("h265") == true -> MimeTypes.VIDEO_H265
					videoMime?.contains("avc") == true || videoMime?.contains("h264") == true -> MimeTypes.VIDEO_H264
					videoMime?.contains("vp8") == true -> MIME_VIDEO_VP8
					videoMime?.contains("vp9") == true -> MimeTypes.VIDEO_VP9
					videoMime?.contains("mp2t") == true || originalMime == MimeTypes.VIDEO_MP2T -> MimeTypes.VIDEO_MP2T
					videoMime?.contains("avi") == true || originalMime == "video/x-msvideo" -> "video/x-msvideo"
					videoMime?.contains("matroska") == true || videoMime?.contains("x-matroska") == true ||
							originalMime == MimeTypes.APPLICATION_MATROSKA -> "video/x-matroska"

					videoMime?.contains("webm") == true || originalMime == MimeTypes.VIDEO_WEBM -> MimeTypes.VIDEO_WEBM
					videoMime?.contains("flv") == true || originalMime == MimeTypes.VIDEO_FLV -> MimeTypes.VIDEO_FLV
					originalMime == MimeTypes.APPLICATION_M3U8 -> MimeTypes.APPLICATION_M3U8
					originalMime == MimeTypes.APPLICATION_MPD -> MimeTypes.APPLICATION_MPD
					audioMime?.contains("truehd") == true ||
							audioMime?.contains("true-hd") == true -> MIME_AUDIO_TRUEHD

					audioMime?.contains("alac") == true -> MIME_AUDIO_ALAC
					audioMime?.contains("dts-hd") == true ||
							audioMime?.contains("dts_hd") == true ||
							audioMime?.contains("vnd.dts.hd") == true -> MimeTypes.AUDIO_DTS_HD

					audioMime?.contains("eac3") == true && audioMime.contains("joc") -> MimeTypes.AUDIO_E_AC3_JOC
					audioMime?.contains("ac3") == true -> MimeTypes.AUDIO_AC3
					audioMime?.contains("eac3") == true -> MimeTypes.AUDIO_E_AC3
					audioMime?.contains("dts") == true -> MimeTypes.AUDIO_DTS
					audioMime?.contains("mpeg") == true ||
							audioMime?.contains("mp3") == true -> MimeTypes.AUDIO_MPEG

					audioMime?.contains("aac") == true -> MimeTypes.AUDIO_AAC
					audioMime?.contains("flac") == true -> MimeTypes.AUDIO_FLAC
					audioMime?.contains("opus") == true -> MimeTypes.AUDIO_OPUS
					audioMime?.contains("vorbis") == true -> MimeTypes.AUDIO_VORBIS
					audioMime?.contains("pcm") == true ||
							audioMime?.contains("lpcm") == true ||
							audioMime?.contains("audio/raw") == true -> MIME_AUDIO_RAW

					audioMime?.contains("amr-wb") == true -> MIME_AUDIO_AMR_WB
					audioMime?.contains("amr") == true -> MIME_AUDIO_AMR
					videoMime?.contains("mp4") == true -> MimeTypes.VIDEO_MP4
					else -> originalMime
				}
				if (finalMime != null) {
					builder.setMimeType(finalMime)
				}
			}
			val metadata = oldItem.mediaMetadata.buildUpon()
				.setMediaType(MediaMetadata.MEDIA_TYPE_MOVIE)
				.build()
			builder.setMediaMetadata(metadata)
			mediaItems.add(builder.build())
		}
		oldPlayer.stop()
		oldPlayer.clearMediaItems()
		newPlayer.setMediaItems(mediaItems, currentItemIndex, playbackPositionMs)
		newPlayer.prepare()
		newPlayer.playWhenReady = playWhenReady
		newPlayer.trackSelectionParameters = oldPlayer.trackSelectionParameters
		session.player = newPlayer
		if (newPlayer is ExoPlayer) {
			player = newPlayer
		}
	}

	override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
		return mediaSession
	}

	override fun onTaskRemoved(rootIntent: Intent?) {
		player.let {
			if (it.playWhenReady) {
				it.pause()
			}
			it.stop()
			it.release()
		}
		ffmpegPlayer.release()
		defaultPlayer.release()
		mediaSession?.release()
		mediaSession = null
		stopSelf()
		super.onTaskRemoved(rootIntent)
	}

	override fun onDestroy() {
		val isTV = (getSystemService(UI_MODE_SERVICE) as? UiModeManager)
			?.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION
		mediaSession?.run {
			player.release()
			ffmpegPlayer.release()
			defaultPlayer.release()
			if (!isTV) castPlayer.release()
			release()
			mediaSession = null
		}
		super.onDestroy()
	}
}
