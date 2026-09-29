package com.example.foodgacha.ui.roll

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodgacha.data.db.entity.TagEntity
import com.example.foodgacha.data.db.model.ItemWithTags
import com.example.foodgacha.data.repository.GalleryRepository
import com.example.foodgacha.domain.reel.CaseMechanics
import com.example.foodgacha.domain.reel.SpinProfile
import com.example.foodgacha.util.SoundManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import kotlin.random.Random

data class RollUiState(
    val items: List<ItemWithTags> = emptyList(),
    val filteredItems: List<ItemWithTags> = emptyList(),
    val allTags: List<TagEntity> = emptyList(),
    val selectedTagIds: Set<Long> = emptySet(),
    val winnerItem: ItemWithTags? = null,
    val reelItems: List<ItemWithTags> = emptyList(),
    val winnerTargetIndex: Int = 0,
    val spinProfile: SpinProfile? = null,
    val isSpinning: Boolean = false,
    val showWinnerDialog: Boolean = false
)

@HiltViewModel
class RollViewModel @Inject constructor(
    private val repository: GalleryRepository,
    private val soundManager: SoundManager
) : ViewModel() {

    private val _selectedTagIds = MutableStateFlow<Set<Long>>(emptySet())
    private val _winnerItem = MutableStateFlow<ItemWithTags?>(null)
    private val _reelItems = MutableStateFlow<List<ItemWithTags>>(emptyList())
    private val _winnerTargetIndex = MutableStateFlow(0)
    private val _spinProfile = MutableStateFlow<SpinProfile?>(null)
    private val _isSpinning = MutableStateFlow(false)
    private val _showWinnerDialog = MutableStateFlow(false)

    val uiState: StateFlow<RollUiState> = combine(
        repository.observeAllItemsWithTags(),
        repository.observeAllTags(),
        _selectedTagIds,
        _winnerItem,
        _reelItems,
        _winnerTargetIndex,
        _spinProfile,
        _isSpinning,
        _showWinnerDialog
    ) { args: Array<Any?> ->
        @Suppress("UNCHECKED_CAST")
        val allItems = args[0] as List<ItemWithTags>
        @Suppress("UNCHECKED_CAST")
        val allTags = args[1] as List<TagEntity>
        @Suppress("UNCHECKED_CAST")
        val selectedTags = args[2] as Set<Long>
        val winner = args[3] as ItemWithTags?
        @Suppress("UNCHECKED_CAST")
        val reel = args[4] as List<ItemWithTags>
        val targetIdx = args[5] as Int
        val profile = args[6] as SpinProfile?
        val spinning = args[7] as Boolean
        val dialog = args[8] as Boolean

        val filtered = if (selectedTags.isEmpty()) {
            allItems
        } else {
            allItems.filter { itemWithTags ->
                itemWithTags.tags.any { selectedTags.contains(it.id) }
            }
        }

        RollUiState(
            items = allItems,
            filteredItems = filtered,
            allTags = allTags,
            selectedTagIds = selectedTags,
            winnerItem = winner,
            reelItems = reel,
            winnerTargetIndex = targetIdx,
            spinProfile = profile,
            isSpinning = spinning,
            showWinnerDialog = dialog
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = RollUiState()
    )

    fun toggleTagFilter(tagId: Long) {
        val current = _selectedTagIds.value.toMutableSet()
        if (current.contains(tagId)) {
            current.remove(tagId)
        } else {
            current.add(tagId)
        }
        _selectedTagIds.value = current
    }

    fun prepareAndStartSpin() {
        val eligible = uiState.value.filteredItems
        if (eligible.isEmpty()) return

        // 1. Uniform random selection (items.random())
        val winner = eligible.random()
        val profile = CaseMechanics.createSpinProfile()

        // 2. Build continuous reel list placing winner at target index
        // Center initial item index is around 2
        val initialCenterIndex = 2
        val targetIndex = initialCenterIndex + profile.tiles

        val totalReelSize = targetIndex + 6
        val reelList = mutableListOf<ItemWithTags>()

        for (i in 0 until totalReelSize) {
            if (i == targetIndex) {
                reelList.add(winner)
            } else {
                // Populate random filler from pool
                reelList.add(eligible.random())
            }
        }

        _winnerItem.value = winner
        _spinProfile.value = profile
        _winnerTargetIndex.value = targetIndex
        _reelItems.value = reelList
        _showWinnerDialog.value = false
        _isSpinning.value = true
        soundManager.playCrateOpen()
    }

    fun playItemScrollSound() {
        soundManager.playItemScroll()
    }

    fun onSpinComplete() {
        _isSpinning.value = false
        _showWinnerDialog.value = true
        val winner = _winnerItem.value
        val rarity = winner?.item?.rarity ?: 0
        soundManager.playReveal(rarity = rarity)
    }


    fun dismissWinnerDialog() {
        _showWinnerDialog.value = false
    }
}
