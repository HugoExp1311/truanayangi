package com.example.foodgacha.di

import android.content.Context
import androidx.room.Room
import com.example.foodgacha.data.db.AppDatabase
import com.example.foodgacha.data.db.dao.ItemDao
import com.example.foodgacha.data.db.dao.TagDao
import com.example.foodgacha.data.repository.GalleryRepository
import com.example.foodgacha.data.scanner.FolderScanner
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideItemDao(database: AppDatabase): ItemDao {
        return database.itemDao()
    }

    @Provides
    fun provideTagDao(database: AppDatabase): TagDao {
        return database.tagDao()
    }

    @Provides
    @Singleton
    fun provideFolderScanner(): FolderScanner {
        return FolderScanner()
    }

    @Provides
    fun provideGalleryRepository(
        database: AppDatabase,
        folderScanner: FolderScanner
    ): GalleryRepository {
        return GalleryRepository(database, folderScanner)
    }

    @Provides
    @Singleton
    fun provideSoundManager(@ApplicationContext context: Context): com.example.foodgacha.util.SoundManager {
        return com.example.foodgacha.util.SoundManager(context)
    }
}
