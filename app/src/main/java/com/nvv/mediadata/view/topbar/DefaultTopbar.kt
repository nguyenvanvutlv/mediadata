package com.nvv.mediadata.view.topbar

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nvv.mediadata.data.Destination

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefaultTopbar(
	destination: Destination,
	openFolder: () -> Unit,
) {
	TopAppBar(
		title = {
			Text(text = destination.label)
		},
		actions = {
			when(destination){
				Destination.LOCAL_FILE -> {
					IconButton(openFolder) {
						Icon(
							imageVector = Icons.Rounded.Folder,
							contentDescription = null,
							modifier = Modifier.size(30.dp)
						)
					}
				}
				Destination.HISTORY -> {

				}
				Destination.NETWORKS -> {

				}
				Destination.SETTINGS -> {

				}
			}
		}
	)
}