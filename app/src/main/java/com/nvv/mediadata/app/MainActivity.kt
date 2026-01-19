package com.nvv.mediadata.app

import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.downloader.PRDownloader
import com.downloader.PRDownloaderConfig
import com.nvv.mediadata.data.Destination
import com.nvv.mediadata.data.provide.rememberNotificationViewModel
import com.nvv.mediadata.data.provide.rememberPlayerViewModel
import com.nvv.mediadata.ui.theme.MediadataTheme
import com.nvv.mediadata.view.core.KeepScreenOn
import com.nvv.mediadata.view.player.PlayerView
import com.nvv.mediadata.view.topbar.DefaultTopbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
	private var pipModeListener: ((isInPipMode: Boolean) -> Unit)? = null
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
		val configPRDownloader = PRDownloaderConfig.newBuilder()
			.setReadTimeout(30_000)
			.setConnectTimeout(30_000)
			.setDatabaseEnabled(true)
			.build()
		PRDownloader.initialize(this, configPRDownloader)
		setContent {
			KeepScreenOn()
			val startDestination = Destination.NETWORKS
			val player = rememberPlayerViewModel()
			val isPlay by player.isPlay.collectAsStateWithLifecycle()
			val notificationViewModel = rememberNotificationViewModel()
			val isOpenNotification by notificationViewModel.isOpen.collectAsStateWithLifecycle()
			val messageNotification by notificationViewModel.message.collectAsStateWithLifecycle()
			val navController = rememberNavController()
			var selectedDestination by rememberSaveable { mutableIntStateOf(startDestination.ordinal) }
			val snackBarHostState = remember { SnackbarHostState() }
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
			}
			LaunchedEffect(isPlay) {
				if (!isPlay) {
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
			MediadataTheme {
				Scaffold(
					topBar = {
						AnimatedVisibility(
							visible = !isPlay,
							enter = slideInVertically(initialOffsetY = { -it }),
							exit = slideOutVertically(targetOffsetY = { -it }),
						) {
							DefaultTopbar(
								destination = Destination.entries[selectedDestination]
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
					}
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