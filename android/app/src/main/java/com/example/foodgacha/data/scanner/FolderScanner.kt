package com.example.foodgacha.data.scanner

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

data class ScannedItem(
    val folderPath: String,
    val folderName: String,
    val images: List<File>
)

@Singleton
class FolderScanner @Inject constructor() {
    private val validExtensions = setOf("jpg", "jpeg", "png", "webp")

    suspend fun scan(motherFolder: File): List<ScannedItem> = withContext(Dispatchers.IO) {
        if (!motherFolder.exists() || !motherFolder.isDirectory) {
            return@withContext emptyList()
        }

        val items = mutableListOf<ScannedItem>()
        scanRecursively(motherFolder, items)
        items
    }

    private fun scanRecursively(currentFolder: File, result: MutableList<ScannedItem>) {
        val files = currentFolder.listFiles() ?: return

        val (subdirectories, regularFiles) = files.partition { it.isDirectory }

        val imageFiles = regularFiles
            .filter { it.isFile && it.extension.lowercase() in validExtensions }
            .sortedBy { it.name.lowercase() }

        if (imageFiles.isNotEmpty()) {
            result.add(
                ScannedItem(
                    folderPath = currentFolder.absolutePath,
                    folderName = currentFolder.name,
                    images = imageFiles
                )
            )
        }

        for (subDir in subdirectories) {
            scanRecursively(subDir, result)
        }
    }
}
