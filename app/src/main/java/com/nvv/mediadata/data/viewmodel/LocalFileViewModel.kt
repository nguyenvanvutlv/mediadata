package com.nvv.mediadata.data.viewmodel

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class LocalFileState(
	val folder: Uri? = null,
	val allFiles: List<DocumentFile> = emptyList(),
	val files: List<DocumentFile> = emptyList(),
	val searchQuery: String = "",
)

@HiltViewModel
class LocalFileViewModel @Inject constructor(
	@ApplicationContext val context: Context
) : ViewModel() {
	private val _localState = MutableStateFlow(LocalFileState())
	val localState = _localState.asStateFlow()

	fun onSearchQueryChanged(query: String) {
		_localState.update { state ->
			val filtered = if (query.isBlank()) {
				state.allFiles
			} else {
				state.allFiles.filter {
					it.name?.contains(query, ignoreCase = true) == true
				}
			}
			state.copy(
				searchQuery = query,
				files = filtered
			)
		}
	}

	fun deleteFile(file: DocumentFile) {
		val success = file.delete()
		if (success) {
			_localState.update { state ->
				val newAllFiles = state.allFiles.filter { it.uri != file.uri }
				val newFiltered = if (state.searchQuery.isBlank()) {
					newAllFiles
				} else {
					newAllFiles.filter {
						it.name?.contains(state.searchQuery, ignoreCase = true) == true
					}
				}
				state.copy(
					allFiles = newAllFiles,
					files = newFiltered
				)
			}
		}
	}

	fun setPath(path: Uri?) {
		_localState.update {
			it.copy(
				folder = path,
				allFiles = emptyList(),
				files = emptyList(),
				searchQuery = "",
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
					allFiles = files,
					files = files
				)
			}
		}
	}
}