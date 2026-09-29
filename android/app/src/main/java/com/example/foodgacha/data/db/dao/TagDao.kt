package com.example.foodgacha.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.foodgacha.data.db.entity.TagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {
    @Query("SELECT * FROM tags ORDER BY name ASC")
    fun observeAllTags(): Flow<List<TagEntity>>

    @Query("SELECT * FROM tags ORDER BY name ASC")
    suspend fun getAllTags(): List<TagEntity>

    @Query("SELECT * FROM tags WHERE name = :name LIMIT 1")
    suspend fun getTagByName(name: String): TagEntity?

    @Query("SELECT * FROM tags WHERE id = :id LIMIT 1")
    suspend fun getTagById(id: Long): TagEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTag(tag: TagEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTags(tags: List<TagEntity>): List<Long>

    @Update
    suspend fun updateTag(tag: TagEntity)

    @Delete
    suspend fun deleteTag(tag: TagEntity)
}
