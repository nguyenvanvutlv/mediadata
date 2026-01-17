package com.nvv.mediadata.view.core.seek


internal fun valueToPx(
	value: Float,
	widthPx: Float,
	range: ClosedFloatingPointRange<Float>
): Float {
	val rangeSIze = range.endInclusive - range.start
	val p = value.coerceIn(range.start, range.endInclusive)
	val progressPercent = (p - range.start) * 100 / rangeSIze
	return (progressPercent * widthPx / 100)
}


internal fun pxToValue(
	position: Float,
	widthPx: Float,
	range: ClosedFloatingPointRange<Float>
): Float {
	val rangeSize = range.endInclusive - range.start
	val percent = position * 100 / widthPx
	return ((percent * (rangeSize) / 100) + range.start).coerceIn(
		range.start,
		range.endInclusive
	)
}

internal fun segmentToPxValues(
	segments: List<Segment>,
	range: ClosedFloatingPointRange<Float>,
	widthPx: Float,
): List<SegmentPxs> {
	val rangeSize = range.endInclusive - range.start
	val sortedSegments = segments.distinct().sortedBy { it.start }
	val segmentStartPxs = sortedSegments.map { segment ->
		val percent = (segment.start - range.start) * 100 / rangeSize
		val startPx = percent * widthPx / 100
		startPx
	}
	return sortedSegments.mapIndexed { index, segment ->
		val endPx =
			if (index != sortedSegments.lastIndex) segmentStartPxs[index + 1] else widthPx
		SegmentPxs(
			name = segment.name,
			color = segment.color,
			startPx = segmentStartPxs[index],
			endPx = endPx
		)
	}
}

internal fun rtlAware(value: Float, widthPx: Float, isRtl: Boolean) =
	if (isRtl) widthPx - value else value

internal fun lerp(start: Float, end: Float, fraction: Float): Float {
	return (1 - fraction) * start + fraction * end
}