package com.nvv.mediadata.view.core

import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext

@Composable
fun KeepScreenOn() {
	val context = LocalContext.current
	DisposableEffect(Unit) {
		val activity = context.findActivity()
		val window = activity?.window
		window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
		onDispose {
			window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
		}
	}
}