package com.example.foodgacha.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "item_images",
    foreignKeys = [
        ForeignKey(
            entity = ItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("itemId")
    ]
)
data class ItemImageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val itemId: Long,
    val filePath: String,
    val fileName: String,
    val dateAdded: Long = System.currentTimeMillis()
)
