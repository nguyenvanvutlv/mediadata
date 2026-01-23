package com.nvv.mediadata.app

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.navigation.compose.rememberNavController
import com.downloader.PRDownloader
import com.downloader.PRDownloaderConfig
import com.nvv.mediadata.data.Destination
import com.nvv.mediadata.data.provide.rememberContext
import com.nvv.mediadata.data.provide.rememberFileViewModel
import com.nvv.mediadata.data.provide.rememberHistoryViewModel
import com.nvv.mediadata.data.provide.rememberNotificationViewModel
import com.nvv.mediadata.data.provide.rememberPlayerViewModel
import com.nvv.mediadata.data.viewmodel.Settings
import com.nvv.mediadata.ui.theme.MediadataTheme
import com.nvv.mediadata.view.core.KeepScreenOn
import com.nvv.mediadata.view.player.PlayerView
import com.nvv.mediadata.view.stream.MiniAudioPlayer
import com.nvv.mediadata.view.topbar.DefaultTopbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
	private var pipModeListener: ((isInPipMode: Boolean) -> Unit)? = null

	private val requestPermissionLauncher = registerForActivityResult(
		ActivityResultContracts.RequestPermission()
	) { isGranted: Boolean ->
		if (isGranted) {

		} else {

		}
	}

	private fun checkNotificationPermission() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			if (ContextCompat.checkSelfPermission(
					this,
					Manifest.permission.POST_NOTIFICATIONS
				) != PackageManager.PERMISSION_GRANTED
			) {
				requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
			}
		}
	}

	fun addOnPictureInPictureModeChangedListener(listener: (Boolean) -> Unit) {
		pipModeListener = listener
	}

	fun removeOnPictureInPictureModeChangedListener() {
		pipModeListener = null
	}

	override fun onPictureInPictureModeChanged(
		isInPictureInPictureMode: Boolean,
		newConfig: Configuration
	) {
		super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
		pipModeListener?.invoke(isInPictureInPictureMode)
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		checkNotificationPermission()
		val configPRDownloader = PRDownloaderConfig.newBuilder()
			.setReadTimeout(30_000)
			.setConnectTimeout(30_000)
			.setDatabaseEnabled(true)
			.build()
		PRDownloader.initialize(this, configPRDownloader)
		setContent {
//			var videoUri by remember { mutableStateOf(intent?.data) }
			var videoUri = "https://dl-z01a-0026.mypikpak.com/download/?fid=zOAqK39MWZ5-b4W9UinoG7FpMpF-V5ioYyPVKop8Vkr5KqjRy6xveCnRXzkAfkDr7po6w3dGvBr1x9TmhpeLy2DoZzT-vUu_tJ2oJhKlEDU=&from=5&verno=3&prod=pikpak&expire=1769277604&g=97B2B3278B5F7D06556A198C4E7AC731F49BF91D&ui=aJgD4HNLZR8dbfo1&t=0&ms=81600000&th=81600000&f=29045508134&alt=0&us=0&hspu=&po=0&fileid=VOjg-o60rV3_BwcKnc2Xl1ERo2&userid=aJgD4HNLZR8dbfo1&pr=XQPkPvr9WWiIuMvELmrVeg59XFTOetZNGO7aLgbQwrok9KnoGoSDrCgF-Fmt4tRwt3KgN07OqPjw866ewr6b47YR8IQsEbgLeg7REDd1xl8rbhHqlgDJhwZMBv7rVDjPXajJr6dadW5m29LWeop04R6v00lHSkjRpldMOUqNO31Eii4wy9hkZJ9C8h2auy9kI1C_zXKPlyTc4xzDVBiKW29aSAb8LPdqAqY7ZRGAZXyZLD6c6LCdFo3Z0qa0KVsELOakZKcXjjIgjPp2aqPfGDBJlB6MyPwKBx4SwnBdRRmCTRz25t2Eqcbkle5veO1PzTmnPJWT8eJXfVytWr9g5WU5iNfC1R-Tkyi-K9N4jVfHMSnumVEubnRZj-Y-yBpnHRuci6DJC2bD1m5Qe_DxRvsP3xRxZX7KKmWowv2LNc-6rfZFE0U-ehl0EJR815xb&sign=794077B66CFAE36565B30EC8BC4D4623"
			val startDestination = Destination.NETWORKS
			val fileViewModel = rememberFileViewModel()
			val player = rememberPlayerViewModel()
			val historyViewModel = rememberHistoryViewModel()
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
					val title = videoUri.toString().substringAfterLast("/")
						.substringBefore("?").ifBlank { "Stream Link" }
					historyViewModel.insertHistory(title, videoUri.toString())
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
				val prefs = context.getSharedPreferences("com.nvv.mediadata", MODE_PRIVATE)
				prefs.registerOnSharedPreferenceChangeListener(listener)
				onDispose {
					prefs.unregisterOnSharedPreferenceChangeListener(listener)
				}
			}
			MediadataTheme(themeMode = themeMode) {
				Box(modifier = Modifier.fillMaxSize()) {
					if (isPlay) {
						KeepScreenOn()
					}
					Scaffold(
						topBar = {
							AnimatedVisibility(
								visible = !isPlay,
								enter = slideInVertically(initialOffsetY = { -it }),
								exit = slideOutVertically(targetOffsetY = { -it }),
							) {
								DefaultTopbar(
									destination = Destination.entries[selectedDestination],
									openFolder = {
										launcherSelectFolder.launch(null)
									}
								)
							}
						},
						bottomBar = {
							AnimatedVisibility(
								visible = !isPlay,
								enter = slideInVertically(initialOffsetY = { it }),
								exit = slideOutVertically(targetOffsetY = { it }),
							) {
								NavigationBar(
									windowInsets = NavigationBarDefaults.windowInsets,
								) {
									Destination.entries.forEachIndexed { index, destination ->
										NavigationBarItem(
											selected = selectedDestination == index,
											onClick = {
												if (selectedDestination != index) {
													navController.navigate(route = destination.route)
													selectedDestination = index
												}
											},
											icon = {
												Icon(
													destination.icon,
													contentDescription = destination.contentDescription
												)
											},
										)
									}
								}
							}
						},
						snackbarHost = {
							SnackbarHost(snackBarHostState)
						},
						contentWindowInsets = WindowInsets(0, 0, 0, 0),
					) {
						Surface(
							modifier = Modifier
								.fillMaxSize()
								.padding(it),
						) {
							Box(
								Modifier.fillMaxSize()
							) {
								AppNavHost(navController, startDestination)
							}
						}
					}

					MiniAudioPlayer(
						visible = isAudioPlaying && !isPlay,
						onNavigateToPlayer = {
							player.toggleVideo(true)
							player.setPlayMode(true)
						},
						modifier = Modifier
							.align(androidx.compose.ui.Alignment.BottomCenter)
							.padding(bottom = 80.dp)
					)

					AnimatedVisibility(
						visible = isPlay,
						modifier = Modifier.fillMaxSize(),
						enter = fadeIn(),
						exit = fadeOut()
					) {
						PlayerView(
							modifier = Modifier
								.fillMaxSize()
								.background(Color.Black),
						)
					}
				}
			}
		}
	}
}