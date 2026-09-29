package com.example.foodgacha.data.db.model

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.foodgacha.data.db.entity.ItemEntity
import com.example.foodgacha.data.db.entity.ItemImageEntity
import com.example.foodgacha.data.db.entity.ItemTagCrossRef
import com.example.foodgacha.data.db.entity.TagEntity
import kotlinx.serialization.Serializable

@Serializable
data class ItemWithTags(
    @Embedded
    val item: ItemEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = ItemTagCrossRef::class,
            parentColumn = "itemId",
            entityColumn = "tagId"
        )
    )
    val tags: List<TagEntity> = emptyList()
)

@Serializable
data class ItemWithDetails(
    @Embedded
    val item: ItemEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = ItemTagCrossRef::class,
            parentColumn = "itemId",
            entityColumn = "tagId"
        )
    )
    val tags: List<TagEntity> = emptyList(),
    @Relation(
        parentColumn = "id",
        entityColumn = "itemId"
    )
    val images: List<ItemImageEntity> = emptyList()
)
