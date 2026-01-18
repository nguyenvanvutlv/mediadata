package com.nvv.mediadata.view.stream

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import com.nvv.mediadata.data.provide.rememberPlayerViewModel

@Composable
fun HomeStream() {
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
					.verticalScroll(rememberScrollState())
			) {
				Row(
					Modifier.fillMaxWidth(),
					verticalAlignment = Alignment.CenterVertically,
					horizontalArrangement = Arrangement.Center
				) {
					OutlinedTextField(
						value = url,
						onValueChange = { n ->
							url = n
						},
						modifier = Modifier
							.fillMaxWidth()
							.padding(horizontal = 16.dp),
						singleLine = true,
						maxLines = 1,
						keyboardActions = KeyboardActions {
							localKeyword?.hide()
							player.setURLs(listOf(url))
							player.selectItem(0)
						},

					)
				}
				Button({
					localKeyword?.hide()
					player.setURLs(listOf(url))
					player.selectItem(0)
				},
					modifier = Modifier.fillMaxWidth()
						.padding(horizontal = 16.dp)
					) {
					Icon(
						imageVector = Icons.Rounded.PlayArrow,
						contentDescription = null
					)
				}
			}
		}
	}
}