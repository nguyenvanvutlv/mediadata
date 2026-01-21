package com.nvv.mediadata.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.ui.graphics.vector.ImageVector

enum class Destination(
	val route: String,
	val label: String,
	val icon: ImageVector,
	val contentDescription: String
) {

	LOCAL_FILE("local", "Local File", Icons.Rounded.Folder, "file"),
	HISTORY("history", "History", Icons.Rounded.History, "history"),
	NETWORKS("network", "Network Stream", Icons.Rounded.Language, "network"),
	SETTINGS("settings", "Settings", Icons.Rounded.Settings, "settings"),
}