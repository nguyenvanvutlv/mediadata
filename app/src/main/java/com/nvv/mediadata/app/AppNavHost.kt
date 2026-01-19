package com.nvv.mediadata.app

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.nvv.mediadata.data.Destination
import com.nvv.mediadata.view.file.LocalFileNavigation
import com.nvv.mediadata.view.settings.SettingNavigation
import com.nvv.mediadata.view.stream.StreamNavigation

@Composable
fun AppNavHost(
	navController: NavHostController,
	startDestination: Destination,
) {
	NavHost(
		navController,
		startDestination = startDestination.route
	) {
		Destination.entries.forEach { destination ->
			composable(destination.route) {
				when (destination) {
					Destination.LOCAL_FILE -> LocalFileNavigation()
					Destination.NETWORKS -> StreamNavigation()
					Destination.SETTINGS -> SettingNavigation()
				}
			}
		}
	}
}