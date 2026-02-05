package com.nvv.mediadata.data.viewmodel


import android.content.Context
import android.os.Handler
import androidx.annotation.OptIn
import androidx.media3.common.Format
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.Renderer
import androidx.media3.exoplayer.audio.AudioRendererEventListener
import androidx.media3.exoplayer.video.VideoRendererEventListener
import com.nvv.mediadata.data.services.HdrCapabilities

@OptIn(UnstableApi::class)
class PriorityRenderersFactory(context: Context) : DefaultRenderersFactory(context) {

	private var ffmpegVideoAvailable = false
	private var ffmpegAudioAvailable = false

	private fun createLoggingVideoListener(delegate: VideoRendererEventListener, hdrCapabilities: HdrCapabilities): VideoRendererEventListener {
		return object : VideoRendererEventListener {
			override fun onVideoDecoderInitialized(decoderName: String, initializedTimestampMs: Long, initializationDurationMs: Long) {
				delegate.onVideoDecoderInitialized(decoderName, initializedTimestampMs, initializationDurationMs)
			}

			override fun onVideoDecoderReleased(decoderName: String) {
				delegate.onVideoDecoderReleased(decoderName)
			}

			override fun onRenderedFirstFrame(output: Any, renderTimeMs: Long) {
				delegate.onRenderedFirstFrame(output, renderTimeMs)
			}

			override fun onVideoEnabled(decoderCounters: androidx.media3.exoplayer.DecoderCounters) {
				delegate.onVideoEnabled(decoderCounters)
			}

			override fun onVideoInputFormatChanged(format: Format, decoderReuseEvaluation: androidx.media3.exoplayer.DecoderReuseEvaluation?) {
				delegate.onVideoInputFormatChanged(format, decoderReuseEvaluation)
			}

			override fun onVideoDisabled(decoderCounters: androidx.media3.exoplayer.DecoderCounters) {
				delegate.onVideoDisabled(decoderCounters)
			}

			override fun onDroppedFrames(count: Int, elapsedMs: Long) {
				delegate.onDroppedFrames(count, elapsedMs)
			}

			override fun onVideoSizeChanged(videoSize: androidx.media3.common.VideoSize) {
				delegate.onVideoSizeChanged(videoSize)
			}

			override fun onVideoFrameProcessingOffset(totalProcessingOffsetUs: Long, frameCount: Int) {
				delegate.onVideoFrameProcessingOffset(totalProcessingOffsetUs, frameCount)
			}
		}
	}

	private fun createLoggingAudioListener(delegate: AudioRendererEventListener): AudioRendererEventListener {
		return object : AudioRendererEventListener {
			override fun onAudioDecoderInitialized(decoderName: String, initializedTimestampMs: Long, initializationDurationMs: Long) {
				delegate.onAudioDecoderInitialized(decoderName, initializedTimestampMs, initializationDurationMs)
			}

			override fun onAudioDecoderReleased(decoderName: String) {
				delegate.onAudioDecoderReleased(decoderName)
			}

			override fun onAudioInputFormatChanged(format: Format, decoderReuseEvaluation: androidx.media3.exoplayer.DecoderReuseEvaluation?) {
				delegate.onAudioInputFormatChanged(format, decoderReuseEvaluation)
			}

			override fun onAudioEnabled(decoderCounters: androidx.media3.exoplayer.DecoderCounters) {
				delegate.onAudioEnabled(decoderCounters)
			}

			override fun onAudioDisabled(decoderCounters: androidx.media3.exoplayer.DecoderCounters) {
				delegate.onAudioDisabled(decoderCounters)
			}

			override fun onAudioSinkError(audioSinkError: Exception) {
				delegate.onAudioSinkError(audioSinkError)
			}


			override fun onAudioUnderrun(bufferSize: Int, bufferSizeMs: Long, elapsedSinceLastFeedMs: Long) {
				delegate.onAudioUnderrun(bufferSize, bufferSizeMs, elapsedSinceLastFeedMs)
			}
		}
	}

	override fun buildVideoRenderers(
		context: Context,
		extensionRendererMode: Int,
		mediaCodecSelector: androidx.media3.exoplayer.mediacodec.MediaCodecSelector,
		enableDecoderFallback: Boolean,
		eventHandler: Handler,
		eventListener: VideoRendererEventListener,
		allowedVideoJoiningTimeMs: Long,
		out: ArrayList<Renderer>
	) {
		val hdrCapabilities = HdrCapabilities.detect(context)
		val loggingListener = createLoggingVideoListener(eventListener, hdrCapabilities)
		if (!hdrCapabilities.canPlayHdr) {
			val ffmpegClassNames = listOf(
				"androidx.media3.decoder.ffmpeg.ExperimentalFfmpegVideoRenderer",
			)
			for (className in ffmpegClassNames) {
				try {
					val ffmpegClass = Class.forName(className)
					val constructor = try {
						ffmpegClass.getConstructor(
							Long::class.java,
							Handler::class.java,
							VideoRendererEventListener::class.java,
							Int::class.java
						)
					} catch (e: NoSuchMethodException) {
						ffmpegClass.getConstructor(
							Long::class.java,
							Handler::class.java,
							VideoRendererEventListener::class.java
						)
					}
					val parameterCount = constructor.parameterTypes.size
					val ffmpegRenderer = if (parameterCount == 4) {
						constructor.newInstance(
							allowedVideoJoiningTimeMs,
							eventHandler,
							loggingListener,
							50
						) as Renderer
					} else {
						constructor.newInstance(
							allowedVideoJoiningTimeMs,
							eventHandler,
							loggingListener
						) as Renderer
					}

					out.add(ffmpegRenderer)
					ffmpegVideoAvailable = true
					break
				} catch (e: ClassNotFoundException) {
				} catch (e: Exception) {
				}
			}
			var av1RendererAvailable = false
			if (!ffmpegVideoAvailable) {
				try {
					val av1Class = Class.forName(
						"androidx.media3.decoder.av1.Libdav1dVideoRenderer",
					)
					val constructor = av1Class.getConstructor(
						Long::class.java,
						Handler::class.java,
						VideoRendererEventListener::class.java,
						Int::class.java
					)
					val av1Renderer = constructor.newInstance(
						allowedVideoJoiningTimeMs,
						eventHandler,
						loggingListener,
						50
					) as Renderer
					out.add(av1Renderer)
					av1RendererAvailable = true
				} catch (e: Exception) {
				}
			} else {
			}
			val defaultRenderers = ArrayList<Renderer>()
			super.buildVideoRenderers(
				context,
				EXTENSION_RENDERER_MODE_OFF,
				mediaCodecSelector,
				enableDecoderFallback,
				eventHandler,
				loggingListener,
				allowedVideoJoiningTimeMs,
				defaultRenderers
			)
			val mediaCodecVideoRenderers = defaultRenderers.filter { renderer ->
				renderer.javaClass.name.contains("MediaCodecVideoRenderer")
			}
			out.addAll(mediaCodecVideoRenderers)
			return
		}
		val ffmpegRenderersForHdr = ArrayList<Renderer>()
		val av1RenderersForHdr = ArrayList<Renderer>()
		val ffmpegClassNames = listOf(
			"androidx.media3.decoder.ffmpeg.ExperimentalFfmpegVideoRenderer",
		)
		for (className in ffmpegClassNames) {
			try {
				val ffmpegClass = Class.forName(className)
				val constructor = try {
					ffmpegClass.getConstructor(
						Long::class.java,
						Handler::class.java,
						VideoRendererEventListener::class.java,
						Int::class.java
					)
				} catch (e: NoSuchMethodException) {
					ffmpegClass.getConstructor(
						Long::class.java,
						Handler::class.java,
						VideoRendererEventListener::class.java
					)
				}

				val parameterCount = constructor.parameterTypes.size
				val ffmpegRenderer = if (parameterCount == 4) {
					constructor.newInstance(
						allowedVideoJoiningTimeMs,
						eventHandler,
						loggingListener,
						50
					) as Renderer
				} else {
					constructor.newInstance(
						allowedVideoJoiningTimeMs,
						eventHandler,
						loggingListener
					) as Renderer
				}

				ffmpegRenderersForHdr.add(ffmpegRenderer)
				ffmpegVideoAvailable = true
				break
			} catch (e: ClassNotFoundException) {
			} catch (e: Exception) {
			}
		}
		if (!ffmpegVideoAvailable) {
			try {
				val av1Class = Class.forName(
					"androidx.media3.decoder.av1.Libdav1dVideoRenderer",
				)
				val constructor = av1Class.getConstructor(
					Long::class.java,
					Handler::class.java,
					VideoRendererEventListener::class.java,
					Int::class.java
				)
				val av1Renderer = constructor.newInstance(
					allowedVideoJoiningTimeMs,
					eventHandler,
					loggingListener,
					50
				) as Renderer
				av1RenderersForHdr.add(av1Renderer)
			} catch (e: Exception) {
			}
		} else {
		}
		val defaultRenderers = ArrayList<Renderer>()
		super.buildVideoRenderers(
			context,
			EXTENSION_RENDERER_MODE_OFF,
			mediaCodecSelector,
			enableDecoderFallback,
			eventHandler,
			loggingListener,
			allowedVideoJoiningTimeMs,
			defaultRenderers
		)

		val mediaCodecVideoRenderers = defaultRenderers.filter { renderer ->
			renderer.javaClass.name.contains("MediaCodecVideoRenderer")
		}
		out.addAll(mediaCodecVideoRenderers)
		out.addAll(ffmpegRenderersForHdr)
		out.addAll(av1RenderersForHdr)
	}

	override fun buildAudioRenderers(
		context: Context,
		extensionRendererMode: Int,
		mediaCodecSelector: androidx.media3.exoplayer.mediacodec.MediaCodecSelector,
		enableDecoderFallback: Boolean,
		audioSink: androidx.media3.exoplayer.audio.AudioSink,
		eventHandler: Handler,
		eventListener: AudioRendererEventListener,
		out: ArrayList<Renderer>
	) {
		val loggingListener = createLoggingAudioListener(eventListener)
		val ffmpegAudioClassNames = listOf(
			"androidx.media3.decoder.ffmpeg.FfmpegAudioRenderer",
		)
		for (className in ffmpegAudioClassNames) {
			try {
				val ffmpegClass = Class.forName(className)
				val constructor = ffmpegClass.getConstructor(
					Handler::class.java,
					AudioRendererEventListener::class.java,
					androidx.media3.exoplayer.audio.AudioSink::class.java
				)
				val ffmpegRenderer = constructor.newInstance(
					eventHandler,
					loggingListener,
					audioSink
				) as Renderer
				out.add(ffmpegRenderer)
				ffmpegAudioAvailable = true
				break
			} catch (e: ClassNotFoundException) {
			} catch (e: Exception) {
			}
		}

		try {
			val av1Class = Class.forName(
				"androidx.media3.decoder.iamf.LibiamfAudioRenderer",
			)
			val constructor = av1Class.getConstructor(
				Handler::class.java,
				AudioRendererEventListener::class.java,
				androidx.media3.exoplayer.audio.AudioSink::class.java
			)
			val av1Renderer = constructor.newInstance(
				eventHandler,
				loggingListener,
				audioSink
			) as Renderer
			out.add(av1Renderer)
		} catch (e: Exception) {
		}
		val defaultRenderers = ArrayList<Renderer>()
		super.buildAudioRenderers(
			context,
			EXTENSION_RENDERER_MODE_OFF,
			mediaCodecSelector,
			enableDecoderFallback,
			audioSink,
			eventHandler,
			loggingListener,
			defaultRenderers
		)
		val mediaCodecAudioRenderers = defaultRenderers.filter { renderer ->
			renderer.javaClass.name.contains("MediaCodecAudioRenderer")
		}
		out.addAll(mediaCodecAudioRenderers)
	}
}