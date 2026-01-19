package com.nvv.mediadata.view.stream

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardBackspace
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.nvv.mediadata.data.provide.rememberPlayerViewModel

@Composable
fun NetworkStream(
	navController: NavHostController,
) {
	var url by remember { mutableStateOf("") }
	val player = rememberPlayerViewModel()
	val localKeyword = LocalSoftwareKeyboardController.current
	Surface(
		Modifier.fillMaxSize()
	) {
		Box(
			Modifier.fillMaxSize()
		) {
			Column(
				Modifier
					.fillMaxSize()
					.verticalScroll(rememberScrollState()),
				horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
			) {
				OutlinedTextField(
					value = url,
					onValueChange = { n -> url = n },
					placeholder = {
						Text(
							"https://stream.mkv",
							style = MaterialTheme.typography.bodyLarge.copy(
								color = Color.DarkGray.copy(alpha = 0.3f)
							),
							maxLines = 1,
							overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
						)
					},
					modifier = Modifier
						.fillMaxWidth()
						.padding(vertical = 10.dp, horizontal = 16.dp),
					singleLine = true,
					maxLines = 1,
					keyboardActions = KeyboardActions {
						localKeyword?.hide()
					},
				)
				Spacer(Modifier.width(16.dp))
				Button(
					onClick = {
						localKeyword?.hide()
						if (url.isNotBlank()) {
							player.setURLs(listOf(url))
							player.selectItem(0)
						}
					},
					modifier = Modifier.fillMaxWidth(0.8f)
				) {
					Text(
						"Play",
						style = MaterialTheme.typography.labelLarge
					)
				}
				Spacer(Modifier.weight(1f))
				Button(
					onClick = {
						navController.popBackStack()
					},
					modifier = Modifier.fillMaxWidth(0.8f)
				) {
					Row(
						Modifier.fillMaxWidth(),
						horizontalArrangement = Arrangement.Center,
						verticalAlignment = Alignment.CenterVertically
					){
						Icon(
							imageVector = Icons.AutoMirrored.Rounded.KeyboardBackspace,
							contentDescription = null,
							modifier = Modifier.size(30.dp)
						)
						Spacer(Modifier.width(5.dp))
						Text(
							"Back",
							style = MaterialTheme.typography.labelLarge
						)
					}
				}
				Spacer(Modifier.height(16.dp))
			}
		}
	}
}