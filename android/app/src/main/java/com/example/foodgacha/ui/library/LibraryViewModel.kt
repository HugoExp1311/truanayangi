package com.example.foodgacha.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodgacha.data.db.entity.TagEntity
import com.example.foodgacha.data.db.model.ItemWithTags
import com.example.foodgacha.data.repository.GalleryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class LibraryUiState(
    val items: List<ItemWithTags> = emptyList(),
    val allTags: List<TagEntity> = emptyList(),
    val selectedTagId: Long? = null,
    val searchQuery: String = ""
)

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repository: GalleryRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedTagId = MutableStateFlow<Long?>(null)

    val uiState: StateFlow<LibraryUiState> = combine(
        repository.observeAllItemsWithTags(),
        repository.observeAllTags(),
        _searchQuery,
        _selectedTagId
    ) { items, tags, query, tagId ->
        val filtered = items.filter { itemWithTags ->
            val matchesQuery = query.isBlank() ||
                itemWithTags.item.name.contains(query, ignoreCase = true) ||
                itemWithTags.item.author.contains(query, ignoreCase = true) ||
                itemWithTags.tags.any { it.name.contains(query, ignoreCase = true) }

            val matchesTag = tagId == null || itemWithTags.tags.any { it.id == tagId }

            matchesQuery && matchesTag
        }

        LibraryUiState(
            items = filtered,
            allTags = tags,
            selectedTagId = tagId,
            searchQuery = query
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LibraryUiState()
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onTagFilterSelected(tagId: Long?) {
        _selectedTagId.value = if (_selectedTagId.value == tagId) null else tagId
    }
}
