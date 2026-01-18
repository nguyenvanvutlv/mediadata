package com.nvv.mediadata.view.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.rounded.ArrowForwardIos
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.nvv.mediadata.view.core.ItemNavigation

@Composable
fun BaseSettingList(
	navController: NavController
){
	Surface(
		modifier = Modifier.fillMaxSize()
	) {
		Box(
			Modifier.fillMaxSize()
		){
			LazyColumn(
				modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
				horizontalAlignment = Alignment.CenterHorizontally
			) {
				item{
					ItemNavigation(
						leading = {
							Icon(
								imageVector = Icons.Rounded.Language,
								contentDescription = null
							)
						},
						trailing = {
							Icon(
								imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
								contentDescription = null
							)
						},
						headline = {
							Text(
								text = "Language",
								style = MaterialTheme.typography.titleMedium
							)
						},
						onClick = {
							navController.navigate("settings/language")
						}
					)
				}
			}
		}
	}
}