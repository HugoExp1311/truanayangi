package com.example.foodgacha.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.foodgacha.data.db.dao.ItemDao
import com.example.foodgacha.data.db.dao.TagDao
import com.example.foodgacha.data.db.entity.ItemEntity
import com.example.foodgacha.data.db.entity.ItemImageEntity
import com.example.foodgacha.data.db.entity.ItemTagCrossRef
import com.example.foodgacha.data.db.entity.TagEntity

@Database(
    entities = [
        ItemEntity::class,
        TagEntity::class,
        ItemTagCrossRef::class,
        ItemImageEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao
    abstract fun tagDao(): TagDao

    companion object {
        const val DATABASE_NAME = "food_gacha.db"
    }
}
