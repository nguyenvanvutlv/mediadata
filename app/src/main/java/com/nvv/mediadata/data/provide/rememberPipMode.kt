package com.nvv.mediadata.data.provide

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.nvv.mediadata.app.MainActivity
import com.nvv.mediadata.view.core.findActivity

@Composable
fun rememberIsInPipMode(): Boolean {
	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
		val activity = LocalContext.current.findActivity() as? MainActivity
		var pipMode by remember {
			mutableStateOf(
				activity?.isInPictureInPictureMode ?: false
			)
		}
		DisposableEffect(activity) {
			val listener: (Boolean) -> Unit = { isInPipMode ->
				pipMode = isInPipMode
			}
			activity?.addOnPictureInPictureModeChangedListener(listener)
			onDispose {
				activity?.removeOnPictureInPictureModeChangedListener()
			}
		}
		return pipMode
	} else {
		return false
	}
}