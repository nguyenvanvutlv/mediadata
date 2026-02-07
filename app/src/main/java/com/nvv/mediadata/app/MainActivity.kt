package com.nvv.mediadata.app

import android.app.PictureInPictureParams
import android.app.UiModeManager
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Rational
import android.view.WindowManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.navigation.compose.rememberNavController
import com.nvv.mediadata.data.Destination
import com.nvv.mediadata.data.provide.rememberContext
import com.nvv.mediadata.data.provide.rememberFileViewModel
import com.nvv.mediadata.data.provide.rememberHistoryViewModel
import com.nvv.mediadata.data.provide.rememberNotificationViewModel
import com.nvv.mediadata.data.provide.rememberPlayerViewModel
import com.nvv.mediadata.data.viewmodel.Settings
import com.nvv.mediadata.view.mobile.MobileRoot
import com.nvv.mediadata.view.tv.TvRoot
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import androidx.activity.enableEdgeToEdge

@OptIn(ExperimentalMaterial3Api::class)
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
	private var pipModeListener: ((isInPipMode: Boolean) -> Unit)? = null
	private var shouldEnterPipOnUserLeave: Boolean = false


	fun addOnPictureInPictureModeChangedListener(listener: (Boolean) -> Unit) {
		pipModeListener = listener
	}

	fun removeOnPictureInPictureModeChangedListener() {
		pipModeListener = null
	}

	fun updatePipState(shouldEnterPip: Boolean) {
		shouldEnterPipOnUserLeave = shouldEnterPip
	}

	override fun onPictureInPictureModeChanged(
		isInPictureInPictureMode: Boolean,
		newConfig: Configuration
	) {
		super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
		pipModeListener?.invoke(isInPictureInPictureMode)
	}

	override fun onUserLeaveHint() {
		if (shouldEnterPipOnUserLeave) {
			val params = PictureInPictureParams.Builder()
				.setAspectRatio(Rational(16, 9))
				.build()
			enterPictureInPictureMode(params)
		}
		super.onUserLeaveHint()
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()
		setContent {
			val isTv = remember {
				(getSystemService(UI_MODE_SERVICE) as? UiModeManager)
					?.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION
			}
			var videoUri by remember { mutableStateOf(intent?.data) }
			val openFromMediaNotification = remember {
				intent?.getBooleanExtra(
					"open_from_media_notification",
					false,
				) ?: false
			}
			val startDestination = Destination.NETWORKS
			val fileViewModel = rememberFileViewModel()
			val player = rememberPlayerViewModel()
			rememberHistoryViewModel()
			val isPlay by player.isPlay.collectAsStateWithLifecycle()
			val notificationViewModel = rememberNotificationViewModel()
			val isOpenNotification by notificationViewModel.isOpen.collectAsStateWithLifecycle()
			val messageNotification by notificationViewModel.message.collectAsStateWithLifecycle()
			val navController = rememberNavController()
			var selectedDestination by rememberSaveable { mutableIntStateOf(startDestination.ordinal) }
			val snackBarHostState = remember { SnackbarHostState() }
			val context = rememberContext()
			var themeMode by remember { mutableStateOf(Settings.getThemeMode(context)) }
			var isAudioPlaying by remember { mutableStateOf(false) }

			LaunchedEffect(openFromMediaNotification) {
				if (openFromMediaNotification) {
					player.toggleVideo(true)
					player.setPlayMode(true)
				}
			}
			LaunchedEffect(player.player.collectAsStateWithLifecycle().value) {
				while (true) {
					player.player.value?.let { p ->
						val hasVideoSelected = p.currentTracks.groups.any { group ->
							group.type == C.TRACK_TYPE_VIDEO && group.isSelected
						}
						isAudioPlaying = (p.isPlaying || p.playbackState == Player.STATE_READY) && !hasVideoSelected
					}
					delay(500)
				}
			}
			val launcherSelectFolder = rememberLauncherForActivityResult(
				contract = ActivityResultContracts.OpenDocumentTree()
			) { uri: Uri? ->
				uri?.let {
					context.contentResolver.takePersistableUriPermission(
						uri,
						Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
					)
					Settings.setPath(context, uri.toString())
					fileViewModel.setPath(uri)
				}
			}
			LaunchedEffect(isOpenNotification) {
				if (isOpenNotification) {
					snackBarHostState.showSnackbar(messageNotification)
					delay(2000L)
					notificationViewModel.close()
				}
			}
			LaunchedEffect(Unit) {
				WindowCompat.setDecorFitsSystemWindows(window, false)
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
					window.attributes.layoutInDisplayCutoutMode = WindowManager
						.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
				}
				val controller =
					WindowInsetsControllerCompat(window, window.decorView)
				controller.systemBarsBehavior =
					WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
				controller.show(WindowInsetsCompat.Type.statusBars())
				controller.show(WindowInsetsCompat.Type.navigationBars())
				try {
					fileViewModel.setPath(
						Settings.getPath(context).toUri()
					)
				} catch (e: Exception) {

				}
				if (videoUri != null) {
					videoUri.toString().substringAfterLast(
						"/",
					)
						.substringBefore("?")
						.ifBlank { "Stream Link" }
					player.setURLs(listOf(videoUri.toString()))
					player.selectItem(0)
				}
			}
			var hasPlayed by rememberSaveable { mutableStateOf(false) }
			LaunchedEffect(isPlay) {
				if (isPlay) {
					hasPlayed = true
				} else {
					if (intent?.action == Intent.ACTION_VIEW && videoUri != null && hasPlayed) {
						finish()
					}
					WindowCompat.setDecorFitsSystemWindows(window, false)
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
						window.attributes.layoutInDisplayCutoutMode = WindowManager
							.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
					}
					val controller = WindowInsetsControllerCompat(window, window.decorView)
					controller.systemBarsBehavior =
						WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
					controller.show(WindowInsetsCompat.Type.statusBars())
					controller.show(WindowInsetsCompat.Type.navigationBars())
				}
			}
			val listener = remember {
				SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
					if (key == "theme_mode") {
						themeMode = Settings.getThemeMode(context)
					}
				}
			}
			DisposableEffect(Unit) {
				val prefs = context.getSharedPreferences(
					"com.nvv.mediadata",
					MODE_PRIVATE,
				)
				prefs.registerOnSharedPreferenceChangeListener(listener)
				onDispose {
					prefs.unregisterOnSharedPreferenceChangeListener(listener)
				}
			}
			val navContent: @Composable () -> Unit = {
				AppNavHost(navController, startDestination)
			}
			if (isTv) {
				TvRoot(
					themeMode = themeMode
				)
			} else {
				MobileRoot(
					navController = navController,
					startDestination = startDestination,
					selectedDestination = selectedDestination,
					onDestinationSelected = { index, _ -> selectedDestination = index },
					onOpenFolder = { launcherSelectFolder.launch(null) },
					isPlay = isPlay,
					themeMode = themeMode,
					snackBarHostState = snackBarHostState,
					videoUri = videoUri,
					isAudioPlaying = isAudioPlaying,
					player = player,
					content = navContent,
				)
			}
		}
	}
}