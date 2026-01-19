package com.nvv.mediadata.view.stream

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.nvv.mediadata.data.provide.rememberPlayerViewModel

@Composable
fun StreamNavigation() {
	val navController = rememberNavController()
	val playerViewModel = rememberPlayerViewModel()
	val player by playerViewModel.player.collectAsStateWithLifecycle()
	val currentBackStackEntry by navController.currentBackStackEntryAsState()
	val currentRoute = currentBackStackEntry?.destination?.route
	
	LaunchedEffect(player) {
		player?.let { p ->
			val hasVideo = p.currentTracks.groups.any { group ->
				group.type == androidx.media3.common.C.TRACK_TYPE_VIDEO
			}
			val isAudioPlaying = (p.isPlaying || p.playbackState == androidx.media3.common.Player.STATE_READY) && !hasVideo
			
			if (isAudioPlaying && currentRoute == "stream") {
				navController.navigate("audio_player") {
					launchSingleTop = true
				}
			}
		}
	}
	
	NavHost(
		navController = navController,
		startDestination = "stream",
		enterTransition = {
			slideInHorizontally(
				initialOffsetX = { it },
				animationSpec = tween(300)
			) + fadeIn(animationSpec = tween(300))
		},
		exitTransition = {
			slideOutHorizontally(
				targetOffsetX = { -it / 3 },
				animationSpec = tween(300)
			) + fadeOut(animationSpec = tween(300))
		},
		popEnterTransition = {
			slideInHorizontally(
				initialOffsetX = { -it / 2 },
				animationSpec = tween(300)
			) + fadeIn(animationSpec = tween(300))
		},
		popExitTransition = {
			slideOutHorizontally(
				targetOffsetX = { it },
				animationSpec = tween(300)
			) + fadeOut(animationSpec = tween(300))
		},
	) {
		composable(route = "stream") {
			BaseStreamNavigation(
				navController
			)
		}
		composable(route = "stream/download") {
			DownloadStream(navController)
		}
		composable(route = "stream/network") {
			NetworkStream(navController)
		}
		composable(route = "stream/audio") {
			AudioStream(navController)
		}
		composable(route = "audio_player") {
			AudioPlayerView(navController)
		}
	}
}