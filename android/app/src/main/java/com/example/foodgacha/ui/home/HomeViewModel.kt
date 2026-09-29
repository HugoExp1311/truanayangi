package com.example.foodgacha.ui.home

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodgacha.data.repository.GalleryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class HomeUiState(
    val motherFolderPath: String = "",
    val isScanning: Boolean = false,
    val scanMessage: String? = null,
    val hasPermission: Boolean = false,
    val totalItemsCount: Int = 0
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: GalleryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        checkPermission()
        viewModelScope.launch {
            repository.observeAllItemsWithTags().collect { items ->
                _uiState.value = _uiState.value.copy(totalItemsCount = items.size)
            }
        }
    }

    fun checkPermission() {
        val granted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            true
        }
        _uiState.value = _uiState.value.copy(hasPermission = granted)
    }

    fun updateFolderPath(path: String) {
        _uiState.value = _uiState.value.copy(motherFolderPath = path, scanMessage = null)
    }

    fun scanFolder() {
        val path = _uiState.value.motherFolderPath.trim()
        if (path.isEmpty()) {
            _uiState.value = _uiState.value.copy(scanMessage = "Please specify a valid folder path.")
            return
        }

        val folder = File(path)
        if (!folder.exists() || !folder.isDirectory) {
            _uiState.value = _uiState.value.copy(scanMessage = "Folder does not exist or is not a directory.")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isScanning = true, scanMessage = null)
            try {
                val count = repository.rescanMotherFolder(path)
                _uiState.value = _uiState.value.copy(
                    isScanning = false,
                    scanMessage = "Successfully scanned $count item folder(s)!"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isScanning = false,
                    scanMessage = "Scan error: ${e.localizedMessage}"
                )
            }
        }
    }

    fun requestStoragePermission(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                    data = Uri.parse("package:${context.packageName}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                val fallbackIntent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(fallbackIntent)
            }
        }
    }
}
