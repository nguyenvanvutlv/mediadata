package com.nvv.mediadata.view.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.nvv.mediadata.data.viewmodel.Settings
import com.nvv.mediadata.data.viewmodel.Settings.Companion.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingTheme(
	navController: NavController,
	onThemeChanged: (ThemeMode) -> Unit
) {
	val context = LocalContext.current
	var selectedTheme by remember { mutableStateOf(Settings.getThemeMode(context)) }

	Scaffold(
		topBar = {
			TopAppBar(
				title = { Text("Theme") },
				navigationIcon = {
					IconButton(onClick = { navController.popBackStack() }) {
						Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
					}
				}
			)
		}
	) { padding ->
		Column(
			modifier = Modifier
				.fillMaxSize()
				.padding(padding)
				.verticalScroll(rememberScrollState())
				.selectableGroup()
		) {
			val options = listOf(
				ThemeMode.SYSTEM to "System Default",
				ThemeMode.LIGHT to "Light Mode",
				ThemeMode.DARK to "Dark Mode"
			)

			options.forEach { (mode, label) ->
				Row(
					modifier = Modifier
						.fillMaxWidth()
						.height(56.dp)
						.selectable(
							selected = (selectedTheme == mode),
							onClick = {
								selectedTheme = mode
								Settings.setThemeMode(context, mode)
								onThemeChanged(mode)
							},
							role = Role.RadioButton
						)
						.padding(horizontal = 16.dp),
					verticalAlignment = Alignment.CenterVertically
				) {
					RadioButton(
						selected = (selectedTheme == mode),
						onClick = null // null recommended for accessibility with selectable
					)
					Spacer(modifier = Modifier.width(16.dp))
					Text(
						text = label,
						style = MaterialTheme.typography.bodyLarge
					)
				}
			}
		}
	}
}