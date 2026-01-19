package com.nvv.mediadata.view.player

import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Build
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.ColorUtils
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.CaptionStyleCompat
import androidx.media3.ui.PlayerView
import com.nvv.mediadata.data.provide.rememberContext
import com.nvv.mediadata.data.provide.rememberPlayerViewModel
import com.nvv.mediadata.view.core.findActivity
import kotlin.math.roundToInt

@OptIn(UnstableApi::class)
@Composable
fun SurfacePlayer(
	modifier: Modifier = Modifier,
	isPipMode: Boolean = false,
) {
	val vm = rememberPlayerViewModel()
	val p by vm.player.collectAsStateWithLifecycle()
	val state by vm.state.collectAsStateWithLifecycle()
	val context = rememberContext()
	val activity = context.findActivity()
	val window = activity?.window
	val lifecycleOwner = LocalLifecycleOwner.current
	val hideSystemBars = {
		window?.let {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
				it.insetsController?.apply {
					hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
					systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
				}
			} else {
				@Suppress("DEPRECATION")
				it.decorView.systemUiVisibility = (
						View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
								or View.SYSTEM_UI_FLAG_FULLSCREEN
								or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
						)
			}
		}
	}
	LaunchedEffect(Unit) {
		hideSystemBars()
	}
	AndroidView(
		modifier = modifier
			.fillMaxSize(),
		factory = {
			PlayerView(context).apply {
				useController = false
				player = p
				resizeMode = state.scaleMode.scaleType
				setShutterBackgroundColor(Color.BLACK)
				setKeepContentOnPlayerReset(true)
				layoutParams = ViewGroup.LayoutParams(
					ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.MATCH_PARENT
				)
			}
		},
		update = { view ->
			if (view.player != p) {
				view.player = p
			}
			val subView = view.subtitleView
			view.resizeMode = state.scaleMode.scaleType
			val opacity = 0.coerceAtLeast(
				state.opacity.coerceAtMost(100));
			val  alpha = (opacity * 255f / 100f).roundToInt();
			val style = CaptionStyleCompat(
				state.subtitleTextColor,
				ColorUtils.setAlphaComponent(Color.BLACK, alpha),
				Color.TRANSPARENT,
				CaptionStyleCompat.EDGE_TYPE_OUTLINE,
				Color.BLACK,
				null
			)
			subView?.setStyle(style)
			if (!isPipMode) {
				subView?.setFixedTextSize(
					TypedValue.COMPLEX_UNIT_SP,
					state.sizeSubtitle
				)
				subView?.setBottomPaddingFraction(state.positionSubtitle)
			}
		}
	)
	DisposableEffect(Unit) {
		activity?.requestedOrientation =
			ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
		val observer = LifecycleEventObserver { _, event ->
			when (event) {
				Lifecycle.Event.ON_RESUME -> {
					hideSystemBars()
				}
				Lifecycle.Event.ON_START -> {
					p?.playWhenReady = true
					p?.prepare()
				}
				Lifecycle.Event.ON_STOP -> {
					p?.playWhenReady = false
				}
				Lifecycle.Event.ON_PAUSE -> {
					//onPipMode()
				}
				else -> Unit
			}
		}
		lifecycleOwner.lifecycle.addObserver(observer)
		onDispose {
			activity?.requestedOrientation =
				ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
			lifecycleOwner.lifecycle.removeObserver(observer)
		}
	}
}