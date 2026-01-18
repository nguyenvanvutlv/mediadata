package com.nvv.mediadata.data.viewmodel

import android.app.PendingIntent
import android.content.Context
import androidx.media3.cast.CastPlayer
import androidx.media3.cast.SessionAvailabilityListener
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
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

class PlaybackService : MediaSessionService() {
	private var mediaSession: MediaSession? = null
	lateinit var player: ExoPlayer
	private lateinit var castPlayer: CastPlayer
	private lateinit var castContext: CastContext
	private var sessionManagerListener: SessionManagerListener<CastSession>? = null
	private fun initializePlayer() {
		val renderersFactory = DefaultRenderersFactory(this)
			.setEnableDecoderFallback(true)
			.setExtensionRendererMode(EXTENSION_RENDERER_MODE_PREFER)
		val trackSelector = DefaultTrackSelector(this)
		val loadControl = DefaultLoadControl.Builder()
			.setBufferDurationsMs(minBuffer,
				maxBuffer,
				bufferForPlayback,
				bufferForPlaybackAfterRebuffer)
			.setBackBuffer(30_000, false)
			.build()
		val dataSourceFactory = DefaultDataSource.Factory(
			this, DefaultHttpDataSource.Factory())
		player = ExoPlayer.Builder(this)
			.setRenderersFactory(renderersFactory)
			.setTrackSelector(trackSelector)
			.setLoadControl(loadControl)
			.setMediaSourceFactory(DefaultMediaSourceFactory(this).setDataSourceFactory(dataSourceFactory))
			.setAudioAttributes(
				AudioAttributes.Builder()
					.setUsage(C.USAGE_MEDIA)
					.setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
					.build(),
				true
			)
			.setHandleAudioBecomingNoisy(true)
			.build()
		val intent = packageManager.getLaunchIntentForPackage(packageName)
		val pendingIntent = PendingIntent.getActivity(
			this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
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
			setupCastListener(castContext)
		} catch (e: Exception) {
			Timber.e(e, "Can't get cast on this device")
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
			val tracks = oldPlayer.currentTracks
			var detectedMimeType: String? = null
			for (group in tracks.groups) {
				if (group.type == C.TRACK_TYPE_VIDEO && group.isSelected) {
					detectedMimeType = group.getTrackFormat(0).sampleMimeType
					break
				}
			}
			if (detectedMimeType != null) {
				val containerMimeType = when {
					detectedMimeType.contains("avc") || detectedMimeType.contains("h264") -> "video/mp4"
					detectedMimeType.contains("hevc") || detectedMimeType.contains("h265") -> "video/mp4"
					detectedMimeType.contains("vp9") -> "video/webm"
					detectedMimeType.contains("matroska") -> "video/x-matroska"
					else -> detectedMimeType
				}
				builder.setMimeType(containerMimeType)
			}
			mediaItems.add(builder.build())
		}
		oldPlayer.stop()
		oldPlayer.clearMediaItems()
		newPlayer.setMediaItems(mediaItems, currentItemIndex, playbackPositionMs)
		newPlayer.prepare()
		newPlayer.playWhenReady = playWhenReady
		session.player = newPlayer
	}

	override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
		return mediaSession
	}

	override fun onDestroy() {
		mediaSession?.run {
			player.release()
			release()
			mediaSession = null
		}
		super.onDestroy()
	}
}