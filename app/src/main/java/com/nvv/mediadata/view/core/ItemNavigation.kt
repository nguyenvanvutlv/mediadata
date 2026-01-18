package com.nvv.mediadata.view.core

import androidx.compose.foundation.clickable
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ItemNavigation(
	leading: @Composable () -> Unit = {},
	trailing: @Composable () -> Unit = {},
	headline: @Composable () -> Unit = {},
	onClick: () -> Unit = {}
){
	ListItem(
		leadingContent = leading,
		headlineContent = headline,
		trailingContent = trailing,
		colors = ListItemDefaults.colors(
			containerColor = MaterialTheme.colorScheme.surface
		),
		modifier = Modifier.clickable(onClick = onClick)
	)
}