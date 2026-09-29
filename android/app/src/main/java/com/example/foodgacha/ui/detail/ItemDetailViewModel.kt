package com.example.foodgacha.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodgacha.data.db.model.ItemWithDetails
import com.example.foodgacha.data.repository.GalleryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ItemDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    repository: GalleryRepository
) : ViewModel() {

    private val itemId: Long = checkNotNull(savedStateHandle["itemId"])

    val itemWithDetails: StateFlow<ItemWithDetails?> = repository.observeItemDetails(itemId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
}
