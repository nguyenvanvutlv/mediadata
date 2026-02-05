package com.nvv.mediadata.view.core.tv

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type

fun Modifier.detectHandleDpadKeyEvents(
	onPressed: () -> Unit = {},
): Modifier = this.then(
	Modifier.onKeyEvent { event ->
		if (event.type == KeyEventType.KeyDown) {
			when (event.key) {
				Key.DirectionUp -> {
					onPressed()
					false
				}

				Key.DirectionDown -> {
					onPressed()
					false
				}

				Key.DirectionLeft -> {
					onPressed()
					false
				}

				Key.DirectionRight -> {
					onPressed()
					false
				}

				Key.DirectionCenter, Key.Enter -> {
					onPressed()
					false
				}

				Key.Back, Key.Backspace, Key.Escape -> {
					onPressed()
					false
				}

				else -> false
			}
		} else false
	}
)