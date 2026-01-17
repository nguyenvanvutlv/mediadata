package com.nvv.mediadata.view.stream

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun StreamNavigation(){
	val navController = rememberNavController()
	NavHost(
		navController = navController,
		startDestination = "home"
	) {
		composable(route = "home"){
			HomeStream()
		}
	}
}