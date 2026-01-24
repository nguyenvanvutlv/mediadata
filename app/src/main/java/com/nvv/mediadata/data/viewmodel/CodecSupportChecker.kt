package com.nvv.mediadata.data.viewmodel

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.mediacodec.MediaCodecInfo
import androidx.media3.exoplayer.mediacodec.MediaCodecUtil
import timber.log.Timber

@OptIn(UnstableApi::class)
object CodecSupportChecker {
	
	data class SupportedCodec(
		val mimeType: String,
		val codecName: String,
		val isHardware: Boolean,
		val isSoftware: Boolean,
		val isSecure: Boolean,
		val isTunneling: Boolean
	)
	
	data class CodecSupportResult(
		val isSupported: Boolean,
		val availableDecoders: List<SupportedCodec>,
		val errorMessage: String? = null
	)
	
	val androidSupportedVideoCodecs = listOf(
		MimeTypes.VIDEO_H263,
		MimeTypes.VIDEO_H264,
		MimeTypes.VIDEO_H265,
		MimeTypes.VIDEO_MP4V,
		MimeTypes.VIDEO_VP8,
		MimeTypes.VIDEO_VP9,
		MimeTypes.VIDEO_AV1,
		MimeTypes.VIDEO_DOLBY_VISION
	)
	
	val androidSupportedAudioCodecs = listOf(
		MimeTypes.AUDIO_AAC,
		MimeTypes.AUDIO_MPEG,
		MimeTypes.AUDIO_OPUS,
		MimeTypes.AUDIO_VORBIS,
		MimeTypes.AUDIO_FLAC,
		MimeTypes.AUDIO_AC3,
		MimeTypes.AUDIO_E_AC3,
		MimeTypes.AUDIO_DTS,
		MimeTypes.AUDIO_AMR_NB,
		MimeTypes.AUDIO_AMR_WB
	)
	
	fun getAllSupportedCodecs(): Map<String, List<String>> {
		val codecs = mutableMapOf<String, MutableList<String>>()
		
		androidSupportedVideoCodecs.forEach { mimeType ->
			try {
				val decoders = MediaCodecUtil.getDecoderInfos(mimeType, false, false)
				val decoderNames = decoders.map { it.name }
				codecs[mimeType] = decoderNames.toMutableList()
				Timber.tag("CodecSupport").d("Video codec $mimeType: ${decoderNames.size} decoders")
			} catch (e: Exception) {
				Timber.tag("CodecSupport").w("Failed to get decoders for $mimeType: ${e.message}")
				codecs[mimeType] = mutableListOf()
			}
		}
		
		androidSupportedAudioCodecs.forEach { mimeType ->
			try {
				val decoders = MediaCodecUtil.getDecoderInfos(mimeType, false, false)
				val decoderNames = decoders.map { it.name }
				codecs[mimeType] = decoderNames.toMutableList()
				Timber.tag("CodecSupport").d("Audio codec $mimeType: ${decoderNames.size} decoders")
			} catch (e: Exception) {
				Timber.tag("CodecSupport").w("Failed to get decoders for $mimeType: ${e.message}")
				codecs[mimeType] = mutableListOf()
			}
		}
		
		return codecs
	}
	
	fun checkCodecSupport(
		mimeType: String,
		requiresSecureDecoder: Boolean = false,
		requiresTunnelingDecoder: Boolean = false
	): CodecSupportResult {
		try {
			val decoders = MediaCodecUtil.getDecoderInfos(
				mimeType,
				requiresSecureDecoder,
				requiresTunnelingDecoder
			)
			
			if (decoders.isEmpty()) {
				val errorMsg = buildString {
					append("Codec không được hỗ trợ: $mimeType\n\n")
					append("Thiết bị không có decoder cho format này.\n")
					append("Các codec được hỗ trợ trên Android:\n")
					androidSupportedVideoCodecs.forEach { codec ->
						append("• $codec\n")
					}
					append("\nVui lòng sử dụng FFmpeg extension để decode.")
				}
				return CodecSupportResult(
					isSupported = false,
					availableDecoders = emptyList(),
					errorMessage = errorMsg
				)
			}
			
			val supportedCodecs = decoders.map { decoderInfo ->
				val decoderName = decoderInfo.name
				val isHardware = decoderName.contains("omx.", ignoreCase = true) ||
					decoderName.contains("c2.", ignoreCase = true) ||
					decoderName.contains("qcom.", ignoreCase = true) ||
					decoderName.contains("mtk.", ignoreCase = true) ||
					decoderName.contains("exynos.", ignoreCase = true) ||
					decoderName.contains("broadcom.", ignoreCase = true) ||
					decoderName.contains("nvidia.", ignoreCase = true) ||
					decoderName.contains("android.", ignoreCase = true)
				
				SupportedCodec(
					mimeType = mimeType,
					codecName = decoderName,
					isHardware = isHardware,
					isSoftware = !isHardware,
					isSecure = decoderInfo.secure,
					isTunneling = decoderInfo.tunneling
				)
			}
			
			Timber.tag("CodecSupport").d("Codec $mimeType: ${supportedCodecs.size} decoders available")
			supportedCodecs.forEach { codec ->
				Timber.tag("CodecSupport").d("  - ${codec.codecName} (hardware: ${codec.isHardware}, secure: ${codec.isSecure}, tunneling: ${codec.isTunneling})")
			}
			
			return CodecSupportResult(
				isSupported = true,
				availableDecoders = supportedCodecs,
				errorMessage = null
			)
		} catch (e: Exception) {
			val errorMsg = buildString {
				append("Lỗi kiểm tra codec: $mimeType\n\n")
				append("Chi tiết: ${e.message}\n\n")
				append("Các codec được hỗ trợ trên Android:\n")
				androidSupportedVideoCodecs.forEach { codec ->
					append("• $codec\n")
				}
			}
			return CodecSupportResult(
				isSupported = false,
				availableDecoders = emptyList(),
				errorMessage = errorMsg
			)
		}
	}
	
	fun checkVideoFormatSupport(
		mimeType: String?,
		codecs: String? = null
	): CodecSupportResult {
		if (mimeType == null) {
			return CodecSupportResult(
				isSupported = false,
				availableDecoders = emptyList(),
				errorMessage = "Không xác định được format video"
			)
		}
		
		val normalizedMimeType = when {
			mimeType.contains("hevc", ignoreCase = true) || 
			mimeType.contains("h265", ignoreCase = true) ||
			codecs?.contains("hev", ignoreCase = true) == true ||
			codecs?.contains("hvc", ignoreCase = true) == true -> MimeTypes.VIDEO_H265
			
			mimeType.contains("avc", ignoreCase = true) ||
			mimeType.contains("h264", ignoreCase = true) ||
			codecs?.contains("avc", ignoreCase = true) == true -> MimeTypes.VIDEO_H264
			
			mimeType.contains("av1", ignoreCase = true) ||
			codecs?.contains("av01", ignoreCase = true) == true -> MimeTypes.VIDEO_AV1
			
			mimeType.contains("vp9", ignoreCase = true) ||
			codecs?.contains("vp09", ignoreCase = true) == true -> MimeTypes.VIDEO_VP9
			
			mimeType.contains("vp8", ignoreCase = true) ||
			codecs?.contains("vp08", ignoreCase = true) == true -> MimeTypes.VIDEO_VP8
			
			mimeType.contains("dolby", ignoreCase = true) ||
			mimeType.contains("vision", ignoreCase = true) ||
			codecs?.contains("dvhe", ignoreCase = true) == true ||
			codecs?.contains("dvh1", ignoreCase = true) == true -> MimeTypes.VIDEO_DOLBY_VISION
			
			mimeType.contains("h263", ignoreCase = true) -> MimeTypes.VIDEO_H263
			mimeType.contains("mp4v", ignoreCase = true) -> MimeTypes.VIDEO_MP4V
			else -> mimeType
		}
		
		return checkCodecSupport(normalizedMimeType)
	}
	
	fun getDeviceCodecInfo(): String {
		val sb = StringBuilder()
		sb.append("=== THÔNG TIN CODEC TRÊN THIẾT BỊ ===\n\n")
		
		val allCodecs = getAllSupportedCodecs()
		
		sb.append("VIDEO CODECS:\n")
		androidSupportedVideoCodecs.forEach { mimeType ->
			val decoders = allCodecs[mimeType] ?: emptyList()
			sb.append("  $mimeType: ${decoders.size} decoder(s)\n")
			decoders.forEach { decoder ->
				sb.append("    - $decoder\n")
			}
		}
		
		sb.append("\nAUDIO CODECS:\n")
		androidSupportedAudioCodecs.forEach { mimeType ->
			val decoders = allCodecs[mimeType] ?: emptyList()
			sb.append("  $mimeType: ${decoders.size} decoder(s)\n")
			decoders.forEach { decoder ->
				sb.append("    - $decoder\n")
			}
		}
		
		return sb.toString()
	}
}
