package com.nvv.mediadata.data.viewmodel

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class LocalFileState(
	val folder: Uri? = null,
	val files: List<DocumentFile> = emptyList()
)

@HiltViewModel
class LocalFileViewModel @Inject constructor(
	@ApplicationContext val context: Context
) : ViewModel(){
	private val _localState = MutableStateFlow(LocalFileState())
	val localState = _localState.asStateFlow()

	fun deleteFile(file: DocumentFile) {
		file.delete()

	}

	fun setPath(path: Uri?) {
		_localState.update {
			it.copy(
				folder = path,
				files = emptyList()
			)
		}
		path?.let {
			val files = _localState.value.files.toMutableList()
			val docFile = DocumentFile.fromTreeUri(
				context,
				it.toString().toUri()
			)
			docFile?.listFiles()?.forEach { f ->
				if (f.isFile) {
					files.add(f)
				}
			}
			_localState.update { local ->
				local.copy(
					files = files
				)
			}
		}
	}
}