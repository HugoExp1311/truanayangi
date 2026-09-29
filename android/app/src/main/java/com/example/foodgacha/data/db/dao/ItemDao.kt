package com.example.foodgacha.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.foodgacha.data.db.entity.ItemEntity
import com.example.foodgacha.data.db.entity.ItemImageEntity
import com.example.foodgacha.data.db.entity.ItemTagCrossRef
import com.example.foodgacha.data.db.model.ItemWithDetails
import com.example.foodgacha.data.db.model.ItemWithTags
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
    @Query("SELECT * FROM items WHERE folderPath = :folderPath LIMIT 1")
    suspend fun getByPath(folderPath: String): ItemEntity?

    @Query("SELECT * FROM items WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ItemEntity?

    @Transaction
    @Query("SELECT * FROM items WHERE id = :id LIMIT 1")
    suspend fun getItemWithDetailsById(id: Long): ItemWithDetails?

    @Transaction
    @Query("SELECT * FROM items WHERE id = :id LIMIT 1")
    fun observeItemWithDetailsById(id: Long): Flow<ItemWithDetails?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ItemEntity): Long

    @Update
    suspend fun updateItem(item: ItemEntity)

    @Delete
    suspend fun deleteItem(item: ItemEntity)

    @Transaction
    @Query("SELECT * FROM items ORDER BY dateAdded DESC")
    fun observeAllItemsWithTags(): Flow<List<ItemWithTags>>

    @Transaction
    @Query("SELECT * FROM items ORDER BY dateAdded DESC")
    suspend fun getAllItemsWithDetails(): List<ItemWithDetails>

    @Query("SELECT * FROM items")
    suspend fun getAllItems(): List<ItemEntity>

    // Image relationships
    @Query("SELECT * FROM item_images WHERE itemId = :itemId ORDER BY fileName ASC")
    suspend fun getImagesForItem(itemId: Long): List<ItemImageEntity>

    @Query("SELECT * FROM item_images WHERE itemId = :itemId ORDER BY fileName ASC")
    fun observeImagesForItem(itemId: Long): Flow<List<ItemImageEntity>>

    @Query("DELETE FROM item_images WHERE itemId = :itemId")
    suspend fun deleteImagesForItem(itemId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImages(images: List<ItemImageEntity>)

    // Tag relationships
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertItemTagCrossRef(crossRef: ItemTagCrossRef)

    @Query("DELETE FROM item_tags WHERE itemId = :itemId")
    suspend fun clearTagsForItem(itemId: Long)

    @Query("DELETE FROM item_tags WHERE itemId = :itemId AND tagId = :tagId")
    suspend fun removeItemTag(itemId: Long, tagId: Long)
}
