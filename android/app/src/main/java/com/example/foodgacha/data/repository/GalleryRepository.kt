package com.example.foodgacha.data.repository

import androidx.room.withTransaction
import com.example.foodgacha.data.db.AppDatabase
import com.example.foodgacha.data.db.entity.ItemEntity
import com.example.foodgacha.data.db.entity.ItemImageEntity
import com.example.foodgacha.data.db.entity.ItemTagCrossRef
import com.example.foodgacha.data.db.entity.TagEntity
import com.example.foodgacha.data.db.model.ItemWithDetails
import com.example.foodgacha.data.db.model.ItemWithTags
import com.example.foodgacha.data.scanner.FolderScanner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GalleryRepository @Inject constructor(
    private val db: AppDatabase,
    private val scanner: FolderScanner
) {
    private val itemDao = db.itemDao()
    private val tagDao = db.tagDao()

    fun observeAllItemsWithTags(): Flow<List<ItemWithTags>> = itemDao.observeAllItemsWithTags()

    fun observeItemDetails(id: Long): Flow<ItemWithDetails?> = itemDao.observeItemWithDetailsById(id)

    suspend fun getItemDetails(id: Long): ItemWithDetails? = itemDao.getItemWithDetailsById(id)

    fun observeAllTags(): Flow<List<TagEntity>> = tagDao.observeAllTags()

    suspend fun getAllTags(): List<TagEntity> = tagDao.getAllTags()

    suspend fun getAllItemsWithDetails(): List<ItemWithDetails> = itemDao.getAllItemsWithDetails()

    /**
     * Rescans mother folder with transactional safety.
     * Preserves existing user-edited metadata (name, author, custom cover, tags).
     */
    suspend fun rescanMotherFolder(motherFolderPath: String): Int = withContext(Dispatchers.IO) {
        val motherFolder = File(motherFolderPath)
        val scannedItems = scanner.scan(motherFolder)
        if (scannedItems.isEmpty()) return@withContext 0

        var count = 0
        db.withTransaction {
            for (scanned in scannedItems) {
                val existing = itemDao.getByPath(scanned.folderPath)
                val defaultCover = scanned.images.firstOrNull()?.name

                val itemId = if (existing == null) {
                    // New item: use folder name as default name, and first image as cover
                    val newItem = ItemEntity(
                        folderPath = scanned.folderPath,
                        folderName = scanned.folderName,
                        name = scanned.folderName,
                        author = "",
                        coverImageFileName = defaultCover,
                        dateAdded = System.currentTimeMillis(),
                        dateUpdated = System.currentTimeMillis()
                    )
                    itemDao.insertItem(newItem)
                } else {
                    // Existing item: preserve user-customized name, author, and custom cover
                    // If existing cover is missing or no longer exists in folder, update to default
                    val coverExists = scanned.images.any { it.name == existing.coverImageFileName }
                    val resolvedCover = if (coverExists) existing.coverImageFileName else defaultCover
                    val updatedItem = existing.copy(
                        folderName = scanned.folderName,
                        coverImageFileName = resolvedCover,
                        dateUpdated = System.currentTimeMillis()
                    )
                    itemDao.updateItem(updatedItem)
                    existing.id
                }

                // Refresh image entities
                itemDao.deleteImagesForItem(itemId)
                val imageEntities = scanned.images.map { imgFile ->
                    ItemImageEntity(
                        itemId = itemId,
                        filePath = imgFile.absolutePath,
                        fileName = imgFile.name,
                        dateAdded = System.currentTimeMillis()
                    )
                }
                itemDao.insertImages(imageEntities)
                count++
            }
        }
        return@withContext count
    }

    suspend fun updateItemMetadata(
        id: Long,
        name: String,
        author: String,
        rarity: Int,
        coverImageFileName: String?,
        selectedTagIds: Set<Long>
    ) = withContext(Dispatchers.IO) {
        db.withTransaction {
            val item = itemDao.getById(id) ?: return@withTransaction
            val updated = item.copy(
                name = name.trim(),
                author = author.trim(),
                rarity = rarity.coerceIn(0, 4),
                coverImageFileName = coverImageFileName,
                dateUpdated = System.currentTimeMillis()
            )
            itemDao.updateItem(updated)

            // Update tags
            itemDao.clearTagsForItem(id)
            for (tagId in selectedTagIds) {
                itemDao.insertItemTagCrossRef(ItemTagCrossRef(itemId = id, tagId = tagId))
            }
        }
    }

    suspend fun getOrCreateTag(name: String): TagEntity = withContext(Dispatchers.IO) {
        val trimmed = name.trim()
        val existing = tagDao.getTagByName(trimmed)
        if (existing != null) return@withContext existing
        val newId = tagDao.insertTag(TagEntity(name = trimmed))
        return@withContext TagEntity(id = newId, name = trimmed)
    }

    suspend fun insertTags(names: List<String>) = withContext(Dispatchers.IO) {
        val distinct = names.map { it.trim() }.filter { it.isNotEmpty() }.distinct()
        val tags = distinct.map { TagEntity(name = it) }
        tagDao.insertTags(tags)
    }

    suspend fun deleteTag(tag: TagEntity) = withContext(Dispatchers.IO) {
        tagDao.deleteTag(tag)
    }
}
