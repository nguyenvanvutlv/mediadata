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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.nvv.mediadata.R
import com.nvv.mediadata.data.provide.rememberPlayerViewModel

@Composable
fun NetworkStream(
	navController: NavHostController,
) {
	var url by remember { mutableStateOf(
		"") }
	val player = rememberPlayerViewModel()
	val localKeyword = LocalSoftwareKeyboardController.current
	
	val streamUrlLabel = stringResource(R.string.stream_url_label)
	val playButtonLabel = stringResource(R.string.play_button_label)
	val networkDisclaimer = stringResource(R.string.network_disclaimer)
	val urlPlaceholder = stringResource(R.string.network_url_placeholder)

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
				horizontalAlignment = Alignment.CenterHorizontally,
			) {
				OutlinedTextField(
					value = url,
					onValueChange = { n -> url = n },
					label = { Text(streamUrlLabel) },
					placeholder = {
						Text(
							urlPlaceholder,
							style = MaterialTheme.typography.bodyLarge.copy(
								color = Color.DarkGray.copy(alpha = 0.3f)
							),
							maxLines = 1,
							overflow = TextOverflow.Ellipsis
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
				Text(
					text = networkDisclaimer,
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
					textAlign = TextAlign.Center,
					modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
				)
				Spacer(Modifier.height(16.dp))
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
						playButtonLabel,
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
							stringResource(R.string.back_button),
							style = MaterialTheme.typography.labelLarge
						)
					}
				}
				Spacer(Modifier.height(16.dp))
			}
		}
	}
}