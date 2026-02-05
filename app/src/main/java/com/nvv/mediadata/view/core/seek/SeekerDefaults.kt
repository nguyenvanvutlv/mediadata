package com.nvv.mediadata.view.core.seek

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object SeekerDefaults {
	@Composable
	fun seekerColors(
		progressColor: Color = Color(0xFFFF4600),
		trackColor: Color = TrackColor,
		disabledProgressColor: Color = Color(0xFFEF7D35)
			.copy(alpha = DisabledProgressAlpha),
		disabledTrackColor: Color = disabledProgressColor
			.copy(alpha = DisabledTrackAlpha)
			.compositeOver(Color.White),
		thumbColor: Color = Color.White,
		disabledThumbColor: Color = Color.White,
		readAheadColor: Color = ReadAheadColor
	): SeekerColors = DefaultSeekerColor(
		progressColor = progressColor,
		trackColor = trackColor,
		disabledProgressColor = disabledProgressColor,
		disabledTrackColor = disabledTrackColor,
		thumbColor = thumbColor,
		disabledThumbColor = disabledThumbColor,
		readAheadColor = readAheadColor
	)


	@Composable
	fun seekerDimensions(
		trackHeight: Dp = TrackHeight,
		progressHeight: Dp = trackHeight,
		thumbRadius: Dp = ThumbRadius,
		gap: Dp = Gap
	): SeekerDimensions = DefaultSeekerDimensions(
		trackHeight = trackHeight,
		progressHeight = progressHeight,
		thumbRadius = thumbRadius,
		gap = gap
	)

	private val TrackColor = Color(0xFFD9D9D9)
	private val ReadAheadColor = Color(0xFFBDBDBD)

	private const val TrackAlpha = 0.24f
	private const val ReadAheadAlpha = 0.44f
	private const val DisabledTrackAlpha = 0.22f
	private const val DisabledProgressAlpha = 0.32f

	internal val ThumbRadius = 10.dp
	private val TrackHeight = 4.dp
	private val Gap = 2.dp

	internal val MinSliderHeight = 48.dp
	internal val MinSliderWidth = ThumbRadius * 2

	internal val ThumbDefaultElevation = 1.dp
	internal val ThumbPressedElevation = 6.dp

	internal val ThumbRippleRadius = 24.dp
}


@Stable
interface SeekerColors {


	@Composable
	fun trackColor(enabled: Boolean): State<Color>


	@Composable
	fun thumbColor(enabled: Boolean): State<Color>


	@Composable
	fun progressColor(enabled: Boolean): State<Color>


	@Composable
	fun readAheadColor(enabled: Boolean): State<Color>
}


@Stable
interface SeekerDimensions {


	@Composable
	fun trackHeight(): State<Dp>


	@Composable
	fun progressHeight(): State<Dp>


	@Composable
	fun gap(): State<Dp>


	@Composable
	fun thumbRadius(): State<Dp>
}

@Immutable
internal class DefaultSeekerDimensions(
	val trackHeight: Dp,
	val progressHeight: Dp,
	val gap: Dp,
	val thumbRadius: Dp
) : SeekerDimensions {
	@Composable
	override fun trackHeight(): State<Dp> {
		return rememberUpdatedState(trackHeight)
	}

	@Composable
	override fun progressHeight(): State<Dp> {
		return rememberUpdatedState(progressHeight)
	}

	@Composable
	override fun gap(): State<Dp> {
		return rememberUpdatedState(gap)
	}

	@Composable
	override fun thumbRadius(): State<Dp> {
		return rememberUpdatedState(thumbRadius)
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other == null || this::class != other::class) return false

		other as DefaultSeekerDimensions

		if (trackHeight != other.trackHeight) return false
		if (progressHeight != other.progressHeight) return false
		if (gap != other.gap) return false
		if (thumbRadius != other.thumbRadius) return false

		return true
	}

	override fun hashCode(): Int {
		var result = trackHeight.hashCode()

		result = 31 * result + progressHeight.hashCode()
		result = 31 * result + gap.hashCode()
		result = 31 * result + thumbRadius.hashCode()

		return result
	}
}

@Immutable
internal class DefaultSeekerColor(
	val progressColor: Color,
	val trackColor: Color,
	val disabledTrackColor: Color,
	val disabledProgressColor: Color,
	val thumbColor: Color,
	val disabledThumbColor: Color,
	val readAheadColor: Color
) : SeekerColors {
	@Composable
	override fun trackColor(enabled: Boolean): State<Color> {
		return rememberUpdatedState(
			if (enabled) trackColor else disabledTrackColor
		)
	}

	@Composable
	override fun thumbColor(enabled: Boolean): State<Color> {
		return rememberUpdatedState(
			if (enabled) thumbColor else disabledThumbColor
		)
	}

	@Composable
	override fun progressColor(enabled: Boolean): State<Color> {
		return rememberUpdatedState(
			if (enabled) progressColor else disabledProgressColor
		)
	}

	@Composable
	override fun readAheadColor(enabled: Boolean): State<Color> {
		return rememberUpdatedState(
			readAheadColor
		)
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other == null || this::class != other::class) return false

		other as DefaultSeekerColor

		if (progressColor != other.progressColor) return false
		if (trackColor != other.trackColor) return false
		if (disabledTrackColor != other.disabledTrackColor) return false
		if (disabledProgressColor != other.disabledProgressColor) return false
		if (thumbColor != other.thumbColor) return false
		if (disabledThumbColor != other.disabledThumbColor) return false
		if (readAheadColor != other.readAheadColor) return false

		return true
	}

	override fun hashCode(): Int {
		var result = progressColor.hashCode()

		result = 31 * result + trackColor.hashCode()
		result = 31 * result + disabledTrackColor.hashCode()
		result = 31 * result + disabledProgressColor.hashCode()
		result = 31 * result + thumbColor.hashCode()
		result = 31 * result + disabledThumbColor.hashCode()
		result = 31 * result + readAheadColor.hashCode()

		return result
	}
}