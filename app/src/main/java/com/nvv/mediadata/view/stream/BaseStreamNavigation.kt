package com.nvv.mediadata.view.stream

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.rounded.Audiotrack
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.NetworkCheck
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.nvv.mediadata.R
import com.nvv.mediadata.view.core.ItemNavigation

@Composable
fun BaseStreamNavigation(
	navController: NavHostController,
){
	val networkStreamTitle = stringResource(R.string.stream_network_title)
	val networkStreamDescription = stringResource(R.string.stream_network_description)
	val audioStreamTitle = stringResource(R.string.stream_audio_title)
	val audioStreamDescription = stringResource(R.string.stream_audio_description)
	val downloadsTitle = stringResource(R.string.stream_downloads_title)
	val downloadsDescription = stringResource(R.string.stream_downloads_description)
	
	Surface(
		Modifier.fillMaxSize()
	) {
		Box(
			Modifier.fillMaxSize()
		){
			LazyColumn(
				Modifier.fillMaxSize(),
				horizontalAlignment = Alignment.CenterHorizontally
			) {
				item{
					ItemNavigation(
						leading = {
							Icon(
								imageVector = Icons.Rounded.NetworkCheck,
								contentDescription = null,
								modifier = Modifier.rotate(45f)
									.size(30.dp)
							)
						},
						trailing = {
							Icon(
								imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
								contentDescription = null,
								modifier = Modifier.size(30.dp)
							)
						},
					headline = {
						Text(
							text = networkStreamTitle,
							style = MaterialTheme.typography.titleMedium
						)
					},
					supportingContent = {
						Text(
							text = networkStreamDescription,
						)
					}
					){
						navController.navigate("stream/network")
					}
				}
				item{
					ItemNavigation(
						leading = {
							Icon(
								imageVector = Icons.Rounded.Audiotrack,
								contentDescription = null,
								modifier = Modifier.size(30.dp)
							)
						},
						trailing = {
							Icon(
								imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
								contentDescription = null,
								modifier = Modifier.size(30.dp)
							)
						},
					headline = {
						Text(
							text = audioStreamTitle,
							style = MaterialTheme.typography.titleMedium
						)
					},
					supportingContent = {
						Text(
							text = audioStreamDescription,
						)
					}
					){
						navController.navigate("stream/audio")
					}
				}
				item{
					ItemNavigation(
						leading = {
							Icon(
								imageVector = Icons.Rounded.Download,
								contentDescription = null,
								modifier = Modifier.size(30.dp)
							)
						},
						trailing = {
							Icon(
								imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
								contentDescription = null,
								modifier = Modifier.size(30.dp)
							)
						},
					headline = {
						Text(
							text = downloadsTitle,
							style = MaterialTheme.typography.titleMedium
						)
					},
					supportingContent = {
						Text(
							text = downloadsDescription,
						)
					}
					){
						navController.navigate("stream/download")
					}
				}
			}
		}
	}
}