package com.example.foodgacha.ui.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodgacha.data.db.entity.ItemImageEntity
import com.example.foodgacha.data.db.entity.TagEntity
import com.example.foodgacha.data.repository.GalleryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ItemEditUiState(
    val id: Long = 0,
    val name: String = "",
    val author: String = "",
    val rarity: Int = 0,
    val folderPath: String = "",
    val coverImageFileName: String? = null,
    val images: List<ItemImageEntity> = emptyList(),
    val selectedTagIds: Set<Long> = emptySet(),
    val allTags: List<TagEntity> = emptyList(),
    val isSaved: Boolean = false,
    val isLoading: Boolean = true
)

@HiltViewModel
class ItemEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: GalleryRepository
) : ViewModel() {

    private val itemId: Long = checkNotNull(savedStateHandle["itemId"])

    private val _uiState = MutableStateFlow(ItemEditUiState(id = itemId))
    val uiState: StateFlow<ItemEditUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val allTags = repository.getAllTags()
            val details = repository.getItemDetails(itemId)
            if (details != null) {
                _uiState.value = ItemEditUiState(
                    id = itemId,
                    name = details.item.name,
                    author = details.item.author,
                    rarity = details.item.rarity,
                    folderPath = details.item.folderPath,
                    coverImageFileName = details.item.coverImageFileName,
                    images = details.images,
                    selectedTagIds = details.tags.map { it.id }.toSet(),
                    allTags = allTags,
                    isLoading = false
                )
            }
        }
    }

    fun onRarityChanged(rarity: Int) {
        _uiState.value = _uiState.value.copy(rarity = rarity.coerceIn(0, 4))
    }

    fun onNameChanged(name: String) {
        _uiState.value = _uiState.value.copy(name = name)
    }

    fun onAuthorChanged(author: String) {
        _uiState.value = _uiState.value.copy(author = author)
    }

    fun onCoverSelected(fileName: String) {
        _uiState.value = _uiState.value.copy(coverImageFileName = fileName)
    }

    fun toggleTag(tagId: Long) {
        val current = _uiState.value.selectedTagIds.toMutableSet()
        if (current.contains(tagId)) {
            current.remove(tagId)
        } else {
            current.add(tagId)
        }
        _uiState.value = _uiState.value.copy(selectedTagIds = current)
    }

    fun addNewTag(tagName: String) {
        val trimmed = tagName.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            val tag = repository.getOrCreateTag(trimmed)
            val updatedAllTags = repository.getAllTags()
            val updatedSelected = _uiState.value.selectedTagIds + tag.id
            _uiState.value = _uiState.value.copy(
                allTags = updatedAllTags,
                selectedTagIds = updatedSelected
            )
        }
    }

    fun saveChanges() {
        val state = _uiState.value
        viewModelScope.launch {
            repository.updateItemMetadata(
                id = state.id,
                name = state.name,
                author = state.author,
                rarity = state.rarity,
                coverImageFileName = state.coverImageFileName,
                selectedTagIds = state.selectedTagIds
            )
            _uiState.value = _uiState.value.copy(isSaved = true)
        }
    }
}
