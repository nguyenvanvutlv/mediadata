package com.nvv.mediadata.view.tv

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nvv.mediadata.data.connecttv.TvPlayRequestStore
import com.nvv.mediadata.data.provide.rememberPlayerViewModel
import com.nvv.mediadata.data.viewmodel.Settings
import com.nvv.mediadata.ui.theme.MediadataTheme
import com.nvv.mediadata.view.player.PlayerViewTV


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TvRoot(
	themeMode: Settings.Companion.ThemeMode,
) {
	val playerViewModel = rememberPlayerViewModel()
	val latestUrl by TvPlayRequestStore.latestUrl.collectAsStateWithLifecycle()
	var hasReceivedUrl by remember { mutableStateOf(false) }
	LaunchedEffect(latestUrl) {
		val url = latestUrl
		if (url.isNullOrBlank()) return@LaunchedEffect
		hasReceivedUrl = true
		playerViewModel.setURLs(listOf(url))
		playerViewModel.selectItem(0)
	}
	MediadataTheme(themeMode = themeMode) {
		Surface(
			modifier = Modifier.fillMaxSize(),
			color = MaterialTheme.colorScheme.background,
		) {
			if (!hasReceivedUrl) {
				Box(
					modifier = Modifier
						.fillMaxSize()
						.padding(32.dp),
					contentAlignment = Alignment.Center,
				) {
					Column(
						modifier = Modifier.fillMaxSize(),
						horizontalAlignment = Alignment.CenterHorizontally,
					) {
						Text(
							text = "Waiting for URL from mobile",
							style = MaterialTheme.typography.headlineMedium,
							textAlign = TextAlign.Center,
						)
						Spacer(modifier = Modifier.height(16.dp))
						Text(
							text = "Open MediaData on your Android phone.\n" +
									"Go to Network Stream (Destination.NETWORKS) and use \"Send URL to TV\".\n" +
									"Make sure this TV and your phone are on the same Wi‑Fi / LAN.",
							style = MaterialTheme.typography.bodyLarge,
							textAlign = TextAlign.Center,
						)
					}
				}
			} else {
				PlayerViewTV(
					modifier = Modifier.fillMaxSize(),
					onBack = {
						playerViewModel.stop()
						hasReceivedUrl = false
						TvPlayRequestStore.onRemotePlayRequested("")
					}
				)
			}
		}
	}
}
