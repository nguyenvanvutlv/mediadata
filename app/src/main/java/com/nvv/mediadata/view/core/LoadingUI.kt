package com.nvv.mediadata.view.core

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LoadingUI(modifier: Modifier = Modifier) {
	Box(modifier = modifier.fillMaxSize()) {
		CircularProgressIndicator(
			modifier = Modifier
				.size(40.dp)
				.align(Alignment.Center)
		)
	}
}