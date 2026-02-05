package com.nvv.mediadata.view.core.tv

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text

@Composable
fun BaseItem(
	title: String,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
	isSelected: Boolean = false,
	subtitle: String? = null,
	leadingContent: @Composable (() -> Unit)? = null,
	trailingContent: @Composable (() -> Unit)? = null,
	enabled: Boolean = true,
	interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
	var isFocused by remember { mutableStateOf(false) }
	val scale by animateFloatAsState(
		targetValue = if (isFocused) 1.02f else 1f,
		animationSpec = tween(durationMillis = 200),
		label = "scale"
	)
	val backgroundColor by animateColorAsState(
		targetValue = if (isFocused) Color(0xFFE8EAED) else Color(0xFF1E2024),
		animationSpec = tween(durationMillis = 200),
		label = "bgColor"
	)
	val contentColor by animateColorAsState(
		targetValue = if (isFocused) Color.Black else Color(0xFFE8EAED),
		animationSpec = tween(durationMillis = 200),
		label = "contentColor"
	)
	val shadowElevation by animateFloatAsState(
		targetValue = if (isFocused) 4f else 0f,
		label = "elevation"
	)
	val shape = RoundedCornerShape(12.dp)
	Box(
		modifier = modifier
			.padding(vertical = 4.dp, horizontal = 0.dp)
			.scale(scale)
			.shadow(
				elevation = shadowElevation.dp,
				shape = shape,
				clip = false
			)
			.background(backgroundColor, shape)
			.border(
				width = if (isSelected && !isFocused) 1.dp else 0.dp,
				color = if (isSelected && !isFocused) Color.White.copy(alpha = 0.5f) else Color.Transparent,
				shape = shape
			)
			.clip(shape)
			.onFocusChanged { isFocused = it.hasFocus }
			.clickable(
				interactionSource = interactionSource,
				indication = null,
				onClick = onClick,
				role = Role.Button,
				enabled = enabled
			)
			.focusable(interactionSource = interactionSource, enabled = enabled)
			.fillMaxWidth()
			.padding(horizontal = 16.dp, vertical = 14.dp)
	) {
		CompositionLocalProvider(LocalContentColor provides contentColor) {
			Row(
				verticalAlignment = Alignment.CenterVertically
			) {
				if (leadingContent != null) {
					Box(modifier = Modifier.padding(end = 12.dp)) {
						leadingContent()
					}
				}

				Column(modifier = Modifier.weight(1f)) {
					Text(
						text = title,
						style = MaterialTheme.typography.titleMedium,
						color = LocalContentColor.current,
						maxLines = 1,
						overflow = TextOverflow.Ellipsis
					)
					if (subtitle != null) {
						Text(
							text = subtitle,
							style = MaterialTheme.typography.bodyLarge,
							color = LocalContentColor.current.copy(alpha = 0.7f),
							maxLines = 1,
							overflow = TextOverflow.Ellipsis
						)
					}
				}

				if (trailingContent != null) {
					Box(modifier = Modifier.padding(start = 12.dp)) {
						trailingContent()
					}
				}
			}
		}
	}
}