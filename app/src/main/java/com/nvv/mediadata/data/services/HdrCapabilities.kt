package com.nvv.mediadata.data.services

import android.content.Context
import android.media.MediaCodecList
import android.media.MediaFormat
import android.os.Build
import android.view.Display

data class HdrCapabilities(
	val supportsHdr10: Boolean,
	val supportsHlg: Boolean,
	val supportsDolbyVision: Boolean,
	val codecSupportsHdr10: Boolean,
	val codecSupportsDolbyVision: Boolean,
	val maxHdrLuminance: Float,
	val canPlayHdr: Boolean,
	val supportedHdrTypes: List<String>,
	val displayHdrSupported: Boolean
) {
	companion object {
		fun detect(context: Context): HdrCapabilities {
			val displayInfo = getDisplayHdrInfo(context)
			val codecInfo = getCodecHdrCapabilities()

			val supportsHdr10 = displayInfo.supportsHdr10 && codecInfo.supportsHdr10
			val supportsHlg = displayInfo.supportsHlg && codecInfo.supportsHdr10
			val supportsDolbyVision = displayInfo.supportsDolbyVision && codecInfo.supportsDolbyVision
			val canPlayHdr = supportsHdr10 || supportsHlg || supportsDolbyVision

			return HdrCapabilities(
				supportsHdr10 = supportsHdr10,
				supportsHlg = supportsHlg,
				supportsDolbyVision = supportsDolbyVision,
				codecSupportsHdr10 = codecInfo.supportsHdr10,
				codecSupportsDolbyVision = codecInfo.supportsDolbyVision,
				maxHdrLuminance = displayInfo.maxHdrLuminance,
				canPlayHdr = canPlayHdr,
				supportedHdrTypes = displayInfo.supportedHdrTypes,
				displayHdrSupported = displayInfo.displayHdrSupported
			)
		}


		private fun getDisplayHdrInfo(context: Context): DisplayHdrInfo {
			return try {
				val display = HdrDisplaySupportChecker.getDisplayForCapabilities(context) ?: return DisplayHdrInfo.default()
				val hdrCapabilities = display.hdrCapabilities ?: return DisplayHdrInfo.default()
				val maxLuminance = hdrCapabilities.desiredMaxLuminance
				val displayHdrSupported = maxLuminance > 0f
				val (typesList, supportsHdr10, supportsHlg, supportsDolbyVision) = when {
					Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> {
						val typesArray = display.mode.supportedHdrTypes
						val list = typesArray.toList()
						Quad(
							list, list.contains(Display.HdrCapabilities.HDR_TYPE_HDR10),
							list.contains(Display.HdrCapabilities.HDR_TYPE_HLG),
							list.contains(Display.HdrCapabilities.HDR_TYPE_DOLBY_VISION)
						)
					}

					else -> Quad(emptyList(), displayHdrSupported, displayHdrSupported, false)
				}
				val typeNames = typesList.map { type ->
					when (type) {
						Display.HdrCapabilities.HDR_TYPE_HDR10 -> "HDR10"
						Display.HdrCapabilities.HDR_TYPE_HLG -> "HLG"
						Display.HdrCapabilities.HDR_TYPE_DOLBY_VISION -> "DolbyVision"
						Display.HdrCapabilities.HDR_TYPE_HDR10_PLUS -> "HDR10+"
						else -> "HDR_$type"
					}
				}
				DisplayHdrInfo(
					supportsHdr10 = supportsHdr10,
					supportsHlg = supportsHlg,
					supportsDolbyVision = supportsDolbyVision,
					maxHdrLuminance = maxLuminance,
					supportedHdrTypes = typeNames,
					displayHdrSupported = displayHdrSupported
				)
			} catch (e: Exception) {
				DisplayHdrInfo.default()
			}
		}


		private data class Quad(
			val list: List<Int>,
			val h10: Boolean,
			val hlg: Boolean,
			val dv: Boolean
		)

		private fun getCodecHdrCapabilities(): CodecHdrInfo {
			var supportsHdr10 = false
			var supportsDolbyVision = false

			try {
				val codecList = MediaCodecList(MediaCodecList.ALL_CODECS)
				for (codecInfo in codecList.codecInfos) {
					if (codecInfo.isEncoder) continue

					val isHardware = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
						!codecInfo.isSoftwareOnly
					} else {
						!codecInfo.name.startsWith("OMX.google.") &&
								!codecInfo.name.startsWith("c2.android.")
					}
					if (!isHardware) continue

					for (mimeType in codecInfo.supportedTypes) {
						if (mimeType == MediaFormat.MIMETYPE_VIDEO_HEVC) {
							try {
								val capabilities = codecInfo.getCapabilitiesForType(mimeType)
								for (profileLevel in capabilities.profileLevels) {
									if (profileLevel.profile == 2) {
										supportsHdr10 = true
										break
									}
								}
							} catch (_: Exception) {
							}
						}
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
							mimeType == MediaFormat.MIMETYPE_VIDEO_AV1
						) {
							supportsHdr10 = true
						}
						if (mimeType == MediaFormat.MIMETYPE_VIDEO_DOLBY_VISION) {
							supportsDolbyVision = true
						}
					}
				}
			} catch (_: Exception) {
			}

			return CodecHdrInfo(
				supportsHdr10 = supportsHdr10,
				supportsDolbyVision = supportsDolbyVision
			)
		}
	}

	private data class DisplayHdrInfo(
		val supportsHdr10: Boolean,
		val supportsHlg: Boolean,
		val supportsDolbyVision: Boolean,
		val maxHdrLuminance: Float,
		val supportedHdrTypes: List<String>,
		val displayHdrSupported: Boolean
	) {
		companion object {
			fun default() = DisplayHdrInfo(
				supportsHdr10 = false,
				supportsHlg = false,
				supportsDolbyVision = false,
				maxHdrLuminance = 0f,
				supportedHdrTypes = emptyList(),
				displayHdrSupported = false
			)
		}
	}

	private data class CodecHdrInfo(
		val supportsHdr10: Boolean,
		val supportsDolbyVision: Boolean
	)
}