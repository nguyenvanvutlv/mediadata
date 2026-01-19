package com.nvv.mediadata.view.core

import androidx.compose.ui.graphics.Color as ComposeColor
import android.graphics.Color as AndroidColor
import androidx.compose.ui.graphics.toArgb

fun AndroidColor.toComposeColor(): ComposeColor {
	return ComposeColor(this.toArgb())
}

fun ComposeColor.toAndroidColor(): AndroidColor {
	return AndroidColor.valueOf(this.toArgb())
}