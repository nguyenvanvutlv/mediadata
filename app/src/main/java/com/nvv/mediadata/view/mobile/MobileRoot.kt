package com.nvv.mediadata.view.mobile

import android.net.Uri
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.nvv.mediadata.data.Destination
import com.nvv.mediadata.data.viewmodel.Settings
import com.nvv.mediadata.ui.theme.MediadataTheme
import com.nvv.mediadata.view.core.KeepScreenOn
import com.nvv.mediadata.view.core.LoadingUI
import com.nvv.mediadata.view.player.PlayerView
import com.nvv.mediadata.view.stream.MiniAudioPlayer
import com.nvv.mediadata.view.topbar.DefaultTopbar


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MobileRoot(
	navController: NavController,
	startDestination: Destination,
	selectedDestination: Int,
	onDestinationSelected: (Int, Destination) -> Unit,
	onOpenFolder: () -> Unit,
	isPlay: Boolean,
	themeMode: Settings.Companion.ThemeMode,
	snackBarHostState: SnackbarHostState,
	videoUri: Uri?,
	isAudioPlaying: Boolean,
	player: com.nvv.mediadata.data.viewmodel.PlayerViewModel,
	content: @Composable () -> Unit,
) {
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
							openFolder = onOpenFolder,
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
											onDestinationSelected(index, destination)
										}
									},
									icon = {
										Icon(
											destination.icon,
											contentDescription = destination.contentDescription,
										)
									},
								)
							}
						}
					}
				},
				snackbarHost = { SnackbarHost(snackBarHostState) },
				contentWindowInsets = WindowInsets(0, 0, 0, 0),
			) { padding ->
				Surface(
					modifier = Modifier
						.fillMaxSize()
						.padding(padding),
				) {
					Box(Modifier.fillMaxSize()) {
						content()
						if (videoUri != null) LoadingUI(Modifier.fillMaxSize())
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
					.align(Alignment.BottomCenter)
					.padding(bottom = 80.dp),
			)

			AnimatedVisibility(
				visible = isPlay,
				modifier = Modifier.fillMaxSize(),
				enter = fadeIn(),
				exit = fadeOut(),
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
