package com.nvv.mediadata.view.stream

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun StreamNavigation() {
	val navController = rememberNavController()
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
		composable(
			route = "stream/network",
		) {
			NetworkStream(navController)
		}
		composable(route = "stream/audio") {
			AudioStream(navController)
		}
	}
}