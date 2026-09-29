package com.example.foodgacha.ui.tags

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodgacha.data.db.entity.TagEntity
import com.example.foodgacha.data.repository.GalleryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject

data class TagsUiState(
    val message: String? = null,
    val isBusy: Boolean = false
)

@HiltViewModel
class TagsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: GalleryRepository
) : ViewModel() {

    val tags: StateFlow<List<TagEntity>> = repository.observeAllTags()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _uiState = MutableStateFlow(TagsUiState())
    val uiState: StateFlow<TagsUiState> = _uiState.asStateFlow()

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    fun addTag(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.getOrCreateTag(name.trim())
            _uiState.value = _uiState.value.copy(message = "Tag added: ${name.trim()}")
        }
    }

    fun deleteTag(tag: TagEntity) {
        viewModelScope.launch {
            repository.deleteTag(tag)
            _uiState.value = _uiState.value.copy(message = "Deleted tag: ${tag.name}")
        }
    }

    fun importTagsFromUri(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isBusy = true)
            try {
                val tagsList = withContext(Dispatchers.IO) {
                    val list = mutableListOf<String>()
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        BufferedReader(InputStreamReader(stream)).useLines { lines ->
                            lines.forEach { line ->
                                // Split by comma if CSV, or take line as is if TXT
                                val parts = line.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                                list.addAll(parts)
                            }
                        }
                    }
                    list
                }
                repository.insertTags(tagsList)
                _uiState.value = _uiState.value.copy(
                    isBusy = false,
                    message = "Successfully imported ${tagsList.size} tag(s)!"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isBusy = false,
                    message = "Error importing tags: ${e.localizedMessage}"
                )
            }
        }
    }

    fun exportMetadataToJson(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isBusy = true)
            try {
                val allDetails = repository.getAllItemsWithDetails()
                val jsonString = json.encodeToString(allDetails)
                withContext(Dispatchers.IO) {
                    context.contentResolver.openOutputStream(uri)?.use { stream ->
                        stream.write(jsonString.toByteArray(Charsets.UTF_8))
                    }
                }
                _uiState.value = _uiState.value.copy(
                    isBusy = false,
                    message = "Successfully exported metadata for ${allDetails.size} item(s)!"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isBusy = false,
                    message = "Error exporting JSON: ${e.localizedMessage}"
                )
            }
        }
    }
}
