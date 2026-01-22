package com.nvv.mediadata.data.viewmodel

import android.app.PendingIntent
import androidx.annotation.OptIn
import androidx.media3.cast.CastPlayer
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON
import androidx.media3.exoplayer.DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.SessionManagerListener
import timber.log.Timber

const val bufferForPlayback = 2_500
const val bufferForPlaybackAfterRebuffer = 3_000
const val maxBuffer = 30_000
const val minBuffer = 6_000

@OptIn(UnstableApi::class)
class PlaybackService : MediaSessionService() {
	private var mediaSession: MediaSession? = null
	lateinit var player: ExoPlayer
	private lateinit var castPlayer: CastPlayer
	private lateinit var castContext: CastContext
	private var sessionManagerListener: SessionManagerListener<CastSession>? = null

	private fun initializePlayer() {
		val renderersFactory = DefaultRenderersFactory(this)
			.setEnableDecoderFallback(true)
			.setExtensionRendererMode(EXTENSION_RENDERER_MODE_ON)
		val trackSelector = DefaultTrackSelector(this)
		val loadControl = DefaultLoadControl.Builder()
			.setBufferDurationsMs(
				minBuffer,
				maxBuffer,
				bufferForPlayback,
				bufferForPlaybackAfterRebuffer
			)
			.setBackBuffer(30_000, false)
			.build()
		val dataSourceFactory = DefaultDataSource.Factory(
			this, DefaultHttpDataSource.Factory()
		)
		player = ExoPlayer.Builder(this)
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
		val preferredLang = Settings.getLanguages(this)
		player.trackSelectionParameters = player.trackSelectionParameters.buildUpon()
			.setPreferredAudioLanguage(preferredLang)
			.setPreferredTextLanguage(preferredLang)
			.build()
		val intent = packageManager.getLaunchIntentForPackage(packageName)
		val pendingIntent = PendingIntent.getActivity(
			this, 0, intent, PendingIntent.FLAG_IMMUTABLE
		)
		mediaSession = MediaSession.Builder(this, player)
			.setSessionActivity(pendingIntent)
			.build()
	}

	override fun onCreate() {
		super.onCreate()
		initializePlayer()
		try {
			castContext = CastContext.getSharedInstance(this)
			castPlayer = CastPlayer.Builder(this).build()
			castPlayer.addListener(object : Player.Listener {
				override fun onTracksChanged(tracks: Tracks) {
					val groups = tracks.groups
					// Audio Selection Logic
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
						}
						else if (group.type == C.TRACK_TYPE_TEXT) {
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
							.contains(C.TRACK_TYPE_TEXT)) {
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
		}
		catch (e: Exception) {
			Timber.e(e)
		}
	}

	private fun setupCastListener(castContext: CastContext) {
		sessionManagerListener = object : SessionManagerListener<CastSession> {
			override fun onSessionStarted(session: CastSession, sessionId: String) {
				swapPlayer(castPlayer)
			}

			override fun onSessionEnded(session: CastSession, error: Int) {
				swapPlayer(player)
			}

			override fun onSessionResumed(session: CastSession, wasSuspended: Boolean) {
				swapPlayer(castPlayer)
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

	private fun swapPlayer(newPlayer: Player) {
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
				for (group in tracks.groups) {
					if (group.isSelected) {
						val format = group.getTrackFormat(0)
						val detectedMime = format.containerMimeType ?: format.sampleMimeType
						if (group.type == C.TRACK_TYPE_VIDEO) {
							videoMime = detectedMime
						} else if (group.type == C.TRACK_TYPE_AUDIO) {
							audioMime = detectedMime
						}
					}
				}

				val originalMime = oldItem.localConfiguration?.mimeType
				val finalMime = when {
					videoMime?.contains("av1") == true -> MimeTypes.VIDEO_AV1
					videoMime?.contains("hevc") == true || videoMime?.contains("h265") == true -> MimeTypes.VIDEO_H265
					videoMime?.contains("avc") == true || videoMime?.contains("h264") == true -> MimeTypes.VIDEO_H264
					videoMime?.contains("vp9") == true -> MimeTypes.VIDEO_VP9
					videoMime?.contains("mp2t") == true || originalMime == MimeTypes.VIDEO_MP2T -> MimeTypes.VIDEO_MP2T
					videoMime?.contains("avi") == true || originalMime == "video/x-msvideo" -> "video/x-msvideo"
					videoMime?.contains("matroska") == true || videoMime?.contains("x-matroska") == true ||
							originalMime == MimeTypes.APPLICATION_MATROSKA -> "video/x-matroska"
					videoMime?.contains("webm") == true || originalMime == MimeTypes.VIDEO_WEBM -> MimeTypes.VIDEO_WEBM
					videoMime?.contains("flv") == true || originalMime == MimeTypes.VIDEO_FLV -> MimeTypes.VIDEO_FLV
					originalMime == MimeTypes.APPLICATION_M3U8 -> MimeTypes.APPLICATION_M3U8
					originalMime == MimeTypes.APPLICATION_MPD -> MimeTypes.APPLICATION_MPD
					audioMime?.contains("ac3") == true -> MimeTypes.AUDIO_AC3
					audioMime?.contains("eac3") == true -> MimeTypes.AUDIO_E_AC3
					audioMime?.contains("dts") == true -> MimeTypes.AUDIO_DTS
					audioMime?.contains("mpeg") == true || audioMime?.contains("mp3") == true -> MimeTypes.AUDIO_MPEG
					audioMime?.contains("aac") == true -> MimeTypes.AUDIO_AAC
					audioMime?.contains("flac") == true -> MimeTypes.AUDIO_FLAC
					audioMime?.contains("opus") == true -> MimeTypes.AUDIO_OPUS
					videoMime?.contains("mp4") == true || (originalMime == null &&
							oldItem.localConfiguration?.uri?.toString()?.contains("http") == true) -> MimeTypes.VIDEO_MP4
					else -> originalMime
				}
				if (finalMime != null) {
					builder.setMimeType(finalMime)
				} else if (oldItem.localConfiguration?.uri?.toString()?.startsWith("http") == true) {
					builder.setMimeType(MimeTypes.VIDEO_MP4)
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
	}

	override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
		return mediaSession
	}

	override fun onTaskRemoved(rootIntent: android.content.Intent?) {
		player.let {
			if (it.playWhenReady) {
				it.pause()
			}
			it.stop()
			it.release()
		}
		mediaSession?.release()
		mediaSession = null
		stopSelf()
		super.onTaskRemoved(rootIntent)
	}

	override fun onDestroy() {
		mediaSession?.run {
			player.release()
			castPlayer.release()
			release()
			mediaSession = null
		}
		super.onDestroy()
	}
}
