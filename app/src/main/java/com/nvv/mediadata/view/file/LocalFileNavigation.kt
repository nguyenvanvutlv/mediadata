package com.nvv.mediadata.view.file

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nvv.mediadata.data.provide.rememberContext
import com.nvv.mediadata.data.provide.rememberFileViewModel
import com.nvv.mediadata.data.provide.rememberPlayerViewModel
import com.nvv.mediadata.data.viewmodel.Settings
import com.nvv.mediadata.view.core.ItemNavigation
import com.nvv.mediadata.view.core.ScrollableText


@Composable
fun LocalFileNavigation() {
	val fileViewModel = rememberFileViewModel()
	val files by fileViewModel.localState.collectAsStateWithLifecycle()
	val playViewModel = rememberPlayerViewModel()
	val context = rememberContext()
	LaunchedEffect(Unit) {
		try {
			fileViewModel.setPath(
				Settings.getPath(context).toUri()
			)
		}catch(e: Exception){

		}
	}
	Surface(
		Modifier.fillMaxSize()
	) {
		Box(
			Modifier.fillMaxSize()
		){
			LazyColumn(
				modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
				verticalArrangement = Arrangement.spacedBy(10.dp),
				horizontalAlignment = Alignment.CenterHorizontally
			) {
				itemsIndexed(files.files) { index, file ->
					ItemNavigation(
						leading = {},
						headline = {
							ScrollableText(
								file.name.toString(),
								modifier = Modifier
							)
						},
						trailing = {}
					){
						val link = file.uri.toString()
						playViewModel.setURLs(
							links = listOf(link)
						)
						playViewModel.selectItem(0)
					}
				}
			}
		}
	}
}