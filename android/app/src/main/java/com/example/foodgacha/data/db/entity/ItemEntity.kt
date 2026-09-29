package com.example.foodgacha.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "items", indices = [Index(value = ["folderPath"], unique = true)])
data class ItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val folderPath: String,
    val folderName: String,
    val name: String,
    val author: String = "",
    val rarity: Int = 0,
    val coverImageFileName: String? = null,
    val dateAdded: Long = System.currentTimeMillis(),
    val dateUpdated: Long = System.currentTimeMillis()
)
