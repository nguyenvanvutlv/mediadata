package com.nvv.mediadata.view.stream

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardBackspace
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.nvv.mediadata.data.provide.rememberContext
import com.nvv.mediadata.data.provide.rememberDownloadFileViewModel
import com.nvv.mediadata.data.provide.rememberFileViewModel
import com.nvv.mediadata.data.viewmodel.Settings

@Composable
fun DownloadStream(
	navController: NavHostController,
){
	val downloadViewModel = rememberDownloadFileViewModel()
	val fileViewModel = rememberFileViewModel()
	val context = rememberContext()
	val launcherSelectFolder = rememberLauncherForActivityResult(
		contract = ActivityResultContracts.OpenDocumentTree()
	) { uri: Uri? ->
		uri?.let {
			context.contentResolver.takePersistableUriPermission(
				uri,
				Intent.FLAG_GRANT_READ_URI_PERMISSION or
						Intent.FLAG_GRANT_WRITE_URI_PERMISSION
			)
			Settings.setPath(context, uri.toString())
			fileViewModel.setPath(uri)
		}
	}
	var url by remember { mutableStateOf(
		"https://dl-a10b-0860.mypikpak.com/download/?fid=dzAb3PY7fX_mtlo7t5Q9tDnSCrSQfHIpuzbNNGkP7XEAQhAN-aNzyuCw9RDolTGcsusJiv093TqEbUGUWV725WDoZzT-vUu_tJ2oJhKlEDU=&from=5&verno=3&prod=pikpak&expire=1768896127&g=61E352640997DF3B6A90E7DD977F01A88288F12B&ui=aJgD4HNLZR8dbfo1&t=0&ms=50400000&th=50400000&f=225806939&alt=0&us=0&hspu=&po=0&fileid=VO_BfI0wnhsxUp6tTFFJQwq3o2&userid=aJgD4HNLZR8dbfo1&pr=XQPkPvr9WWiIuMvELmrVeohtPvKl8DG3_2uxmakyO_c6AFhiIcGLYk9s0MPZV3TlroWSewoqTA6XcLSM0s6Gjh-_NNpC_NYwMiF75_OcaIUeJwxuvqmIHihAiNOx6ydFtmUaBG5djAKdKGlkgJxSIx6v00lHSkjRpldMOUqNO31Eii4wy9hkZJ9C8h2auy9kanc-z0Pz9f__wM-zo5Mi4uB6swAQveONOF0tamqP_sofy6awhtsqTE26s1U82zme1JfnXl3kJ8TBmyZmbjShRBQlM_gZLb5vd6iq-J_E_Uu78VMyLFvGBtCn0sYF1UxM5jU94LwxEcDqnxH3s35puVaZ0cRi00WzbNhO3997KEH4bvTiVGKTN7YL5XHUT4FXthn1hS8lbQZVt73Tkqx2ijuUMzbNUVgcW0wrGGyg0catPpQd5s_XeYntrZnsNEqX&sign=0E2852D591ED9886176AAA98060F6E4A") }
	var title by remember { mutableStateOf("") }
	val localKeyword = LocalSoftwareKeyboardController.current
	Surface(
		Modifier.fillMaxSize()
	) {
		Box(
			Modifier.fillMaxSize()
		) {
			Column(
				Modifier
					.fillMaxSize()
					.verticalScroll(rememberScrollState()),
				horizontalAlignment = Alignment.CenterHorizontally,
			) {
				OutlinedTextField(
					value = url,
					onValueChange = { n -> url = n },
					label = { Text("Stream URL") },
					placeholder = {
						Text(
							"https://stream.mkv",
							style = MaterialTheme.typography.bodyLarge.copy(
								color = Color.DarkGray.copy(alpha = 0.3f)
							),
							maxLines = 1,
							overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
						)
					},
					modifier = Modifier
						.fillMaxWidth()
						.padding(top = 10.dp, start = 16.dp, end = 16.dp, bottom = 5.dp),
					singleLine = true,
					maxLines = 1,
					keyboardActions = KeyboardActions {
						localKeyword?.hide()
					},
				)
				OutlinedTextField(
					value = title,
					onValueChange = { n -> title = n },
					label = { Text("File Name (Optional)") },
					placeholder = {
						Text(
							"My Video",
							style = MaterialTheme.typography.bodyLarge.copy(
								color = Color.DarkGray.copy(alpha = 0.3f)
							)
						)
					},
					modifier = Modifier
						.fillMaxWidth()
						.padding(top = 5.dp, start = 16.dp, end = 16.dp, bottom = 10.dp),
					singleLine = true,
					maxLines = 1,
					keyboardActions = KeyboardActions {
						localKeyword?.hide()
					},
				)
				Spacer(Modifier.width(16.dp))
				Button(
					onClick = {
						localKeyword?.hide()
						if (Settings.getPath(context).trim().isEmpty()){
							launcherSelectFolder.launch(null)
							return@Button
						}
						downloadViewModel.startDownloadFile(url, title.ifBlank { null })
						navController.popBackStack()
					},
					modifier = Modifier.fillMaxWidth(0.8f)
				) {
					Row(
						Modifier.fillMaxWidth(),
						horizontalArrangement = Arrangement.Center,
						verticalAlignment = Alignment.CenterVertically
					){
						Icon(
							imageVector = Icons.Rounded.Download,
							contentDescription = null,
							modifier = Modifier.size(30.dp)
						)
						Spacer(Modifier.width(5.dp))
						Text(
							"Download",
							style = MaterialTheme.typography.labelLarge
						)
					}
				}
				Spacer(Modifier.weight(1f))
				Button(
					onClick = {
						navController.popBackStack()
					},
					modifier = Modifier.fillMaxWidth(0.8f)
				) {
					Row(
						Modifier.fillMaxWidth(),
						horizontalArrangement = Arrangement.Center,
						verticalAlignment = Alignment.CenterVertically
					){
						Icon(
							imageVector = Icons.AutoMirrored.Rounded.KeyboardBackspace,
							contentDescription = null,
							modifier = Modifier.size(30.dp)
						)
						Spacer(Modifier.width(5.dp))
						Text(
							"Back",
							style = MaterialTheme.typography.labelLarge
						)
					}
				}
				Spacer(Modifier.height(16.dp))
			}
		}
	}
}
