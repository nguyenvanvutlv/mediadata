package com.nvv.mediadata.view.topbar

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import com.nvv.mediadata.data.Destination

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefaultTopbar(
	destination: Destination
){
	TopAppBar(
		title = {
			Text(text = destination.label)
		},
		actions = {

		}
	)
}