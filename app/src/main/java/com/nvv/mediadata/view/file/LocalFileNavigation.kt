package com.nvv.mediadata.view.file

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.FolderOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nvv.mediadata.R
import com.nvv.mediadata.data.provide.rememberContext
import com.nvv.mediadata.data.provide.rememberFileViewModel
import com.nvv.mediadata.data.provide.rememberPlayerViewModel
import com.nvv.mediadata.data.viewmodel.Settings
import com.nvv.mediadata.view.core.ItemNavigation
import com.nvv.mediadata.view.core.ScrollableText
import kotlinx.coroutines.launch


@Composable
fun LocalFileNavigation() {
	val fileViewModel = rememberFileViewModel()
	val files by fileViewModel.localState.collectAsStateWithLifecycle()
	val playViewModel = rememberPlayerViewModel()
	val context = rememberContext()
	var fileToDelete by remember { mutableStateOf<DocumentFile?>(null) }
	var showDeleteDialog by remember { mutableStateOf(false) }
	val deleteTitle = stringResource(R.string.delete_file_title)
	val deleteMessage = stringResource(R.string.delete_file_message)
	val deleteButton = stringResource(R.string.delete_button)
	val cancelButton = stringResource(R.string.cancel_button)
	val scope = rememberCoroutineScope()
	LaunchedEffect(Unit) {
		try {
			fileViewModel.setPath(
				Settings.getPath(context).toUri()
			)
		} catch (e: Exception) {

		}
	}
	if (showDeleteDialog && fileToDelete != null) {
		AlertDialog(
			onDismissRequest = { showDeleteDialog = false },
			title = { Text(deleteTitle) },
			text = { Text(deleteMessage) },
			confirmButton = {
				TextButton(
					onClick = {
						fileToDelete?.let { fileViewModel.deleteFile(it) }
						showDeleteDialog = false
						fileToDelete = null
					}
				) {
					Text(deleteButton, color = MaterialTheme.colorScheme.error)
				}
			},
			dismissButton = {
				TextButton(onClick = { showDeleteDialog = false }) {
					Text(cancelButton)
				}
			}
		)
	}
	Surface(
		Modifier.fillMaxSize()
	) {
		Column(
			Modifier.fillMaxSize()
		) {
			OutlinedTextField(
				value = files.searchQuery,
				onValueChange = { fileViewModel.onSearchQueryChanged(it) },
				modifier = Modifier
					.fillMaxWidth()
					.padding(16.dp),
				placeholder = { Text("Search files...") },
				leadingIcon = {
					Icon(Icons.Default.Search, contentDescription = null)
				},
				singleLine = true,
				shape = RoundedCornerShape(12.dp)
			)

			Box(
				Modifier.weight(1f)
			) {
				if (files.files.isEmpty()) {
					Column(
						modifier = Modifier.fillMaxSize(),
						verticalArrangement = Arrangement.Center,
						horizontalAlignment = Alignment.CenterHorizontally
					) {
						Icon(
							imageVector = Icons.Rounded.FolderOff,
							contentDescription = null,
							modifier = Modifier.size(64.dp),
							tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
						)
						Spacer(modifier = Modifier.height(16.dp))
						Text(
							text = "No files found",
							style = MaterialTheme.typography.titleMedium,
							color = MaterialTheme.colorScheme.onSurfaceVariant
						)
						Spacer(modifier = Modifier.height(8.dp))
						Text(
							text = if (files.searchQuery.isNotEmpty()) "Try a different search query" else "This folder is empty",
							style = MaterialTheme.typography.bodyMedium,
							color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
						)
					}
				} else {
					LazyColumn(
						modifier = Modifier
							.fillMaxSize()
							.padding(horizontal = 16.dp, vertical = 8.dp),
						verticalArrangement = Arrangement.spacedBy(8.dp),
						horizontalAlignment = Alignment.CenterHorizontally
					) {
						itemsIndexed(files.files) { index, file ->
							Card(
								modifier = Modifier.fillMaxSize(),
								elevation = CardDefaults.cardElevation(
									defaultElevation = 2.dp
								),
								colors = CardDefaults.cardColors(
									containerColor = MaterialTheme.colorScheme.surfaceVariant
								)
							) {
								ItemNavigation(
									leading = {},
									headline = {
										ScrollableText(
											file.name.toString(),
											modifier = Modifier
										)
									},
									trailing = {
										IconButton(
											onClick = {
												fileToDelete = file
												showDeleteDialog = true
											}
										) {
											Icon(
												imageVector = Icons.Rounded.Delete,
												contentDescription = deleteButton,
												tint = MaterialTheme.colorScheme.error,
												modifier = Modifier.size(24.dp)
											)
										}
									}
								) {
									scope.launch {
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
			}
		}
	}
}