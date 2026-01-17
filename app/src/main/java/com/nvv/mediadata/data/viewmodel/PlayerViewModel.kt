package com.nvv.mediadata.data.viewmodel

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.Tracks
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import com.nvv.mediadata.data.model.PlaybackState
import com.nvv.mediadata.data.model.VideoScaleMode
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.File
import javax.inject.Inject
import kotlin.math.max

const val bufferForPlayback = 2_500
const val bufferForPlaybackAfterRebuffer = 3_000
const val maxBuffer = 30_000
const val minBuffer = 6_000

@HiltViewModel
class PlayerViewModel @Inject constructor(
	@ApplicationContext private val context: Context,
) : ViewModel() {
	private var _isPlay = MutableStateFlow(false)
	val isPlay = _isPlay.asStateFlow()
	private fun buildMedia3ExoPlayer(): ExoPlayer {
		val renderersFactory = DefaultRenderersFactory(
			context.applicationContext
		)
			.setEnableDecoderFallback(true)
			.setExtensionRendererMode(EXTENSION_RENDERER_MODE_PREFER)
		val ts: DefaultTrackSelector = DefaultTrackSelector(context.applicationContext)
			.apply {
				setParameters(
					buildUponParameters()
				)
			}
		val loadControl = DefaultLoadControl.Builder()
			.setBufferDurationsMs(
				minBuffer,
				maxBuffer,
				bufferForPlayback,
				bufferForPlaybackAfterRebuffer
			)
			.setBackBuffer(
				30_000,
				false
			)
			.setPrioritizeTimeOverSizeThresholds(false)
			.build()
		val httpFactory: DataSource.Factory =
			DefaultHttpDataSource.Factory()
		val dataSourceFactory: DataSource.Factory =
			DefaultDataSource.Factory(context.applicationContext, httpFactory)
		val player = ExoPlayer.Builder(context.applicationContext)
			.setRenderersFactory(renderersFactory)
			.setTrackSelector(ts)
			.setAudioAttributes(
				AudioAttributes.Builder()
					.setUsage(C.USAGE_MEDIA)
					.setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
					.build(),
				true
			)
			.setHandleAudioBecomingNoisy(true)
			.setLoadControl(loadControl)
			.setMediaSourceFactory(
				DefaultMediaSourceFactory(context.applicationContext)
					.setDataSourceFactory(dataSourceFactory)
			)
			.build()
		return player
	}

	///////// STATE PLAYER /////////
	private val _items = MutableStateFlow<List<MediaItem>>(
		emptyList()
	)
	val items = _items.asStateFlow()
	private val _index = MutableStateFlow(0)
	val currentMediaIndex = _index.asStateFlow()
	private val _player = MutableStateFlow(
		buildMedia3ExoPlayer()
	)
	val player = _player.asStateFlow()
	private var _state = MutableStateFlow(PlaybackState())
	val state = _state.asStateFlow()

	private var bufferUpdateHandler: Handler? = null
	private var _playerListener: Player.Listener
	private val handler = Handler(Looper.getMainLooper())
	private val progressRunnable = object : Runnable {
		override fun run() {
			_state.update {
				it.copy(
					position = _player.value.currentPosition,
					duration = _player.value.duration
				)
			}
			if (_state.value.startWith > 0) {
				//seekTo(_state.value.startWith.toDouble() / 100f)
				_state.update {
					it.copy(
						startWith = 0L
					)
				}
			}
			if (_player.value.isPlaying) {
				handler.postDelayed(this, 500L)
			}
		}
	}

	///////// STATE PLAYER /////////

	init {
		this._playerListener = this.createListener()
		this._player.value.addListener(this._playerListener)
	}

	private fun createListener(): Player.Listener {
		return object : Player.Listener {
			override fun onTimelineChanged(timeline: Timeline, reason: Int) {
				super.onTimelineChanged(timeline, reason)
				_index.value = player.value.currentMediaItemIndex
			}

			override fun onTracksChanged(tracks: Tracks) {
				super.onTracksChanged(tracks)
			}

			override fun onIsPlayingChanged(isPlaying: Boolean) {
				super.onIsPlayingChanged(isPlaying)
				_state.update {
					it.copy(
						isPlaying = isPlaying
					)
				}
				if (isPlaying) {
					handler.removeCallbacks(progressRunnable)
					handler.post(progressRunnable)
				} else {
					handler.removeCallbacks(progressRunnable)
				}
			}

			override fun onPlaybackStateChanged(playbackState: Int) {
				super.onPlaybackStateChanged(playbackState)
				if (playbackState == Player.STATE_READY) {

				}
				_state.update {
					it.copy(
						isBuffering = playbackState == Player.STATE_BUFFERING,
					)
				}
				if (playbackState == Player.STATE_BUFFERING) {

				}
			}

			override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
				super.onMediaItemTransition(mediaItem, reason)
				_index.value = player.value.currentMediaItemIndex
			}

			override fun onPlayerError(error: PlaybackException) {
				super.onPlayerError(error)
			}
		}
	}

	private fun String.toMediaUri(): Uri {
		return when {
			startsWith("http://", true) ||
					startsWith("https://", true) ||
					startsWith("content://", true) ||
					startsWith("file://", true) ->
				Uri.parse(this)
			else ->
				Uri.fromFile(File(this)) // local path
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
			.apply {
				mimeType?.let { setMimeType(it) }
			}
			.build()
	}

	fun setURLs(links: List<String>) {
		this._state.value = PlaybackState()
		this@PlayerViewModel._items.value = links.map {
			buildMediaItem(it)
		}
		this@PlayerViewModel._player
			.value.setMediaItems(this@PlayerViewModel._items.value)
	}

	fun setURL(link: String) {
		this.setURLs(listOf(link))
	}

	fun selectItem(index: Int) {
		_isPlay.value = true
		if (index !in this._items.value.indices) return
		this._index.value = index
		handler.removeCallbacks(progressRunnable)
		this._player.value.seekTo(index, 0L)
		this._player.value.prepare()
		this._player.value.playWhenReady = true
		this._player.value.removeListener(this._playerListener)
		this._playerListener = this.createListener()
		this._player.value.addListener(this._playerListener)
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
	}
	fun seekTo(progress: Double) {
		val newPosition = max(
			0L,
			(progress * this._state.value.duration).toLong()
		)
		this._player.value.seekTo(
			newPosition.coerceAtLeast(0L)
		)
	}


	fun stop() {
		_isPlay.value = false
		this.handler.removeCallbacks(progressRunnable)
		this._player.value.removeListener(this._playerListener)
		this._player.value.stop()
		this._player.value.clearMediaItems()
		this._items.value = emptyList()
	}
}