package com.dungeoncrawler.wearos.di

import android.content.Context
import androidx.room.Room
import com.dungeoncrawler.wearos.data.local.db.AppDatabase
import com.dungeoncrawler.wearos.data.local.db.dao.EquipmentDao
import com.dungeoncrawler.wearos.data.local.db.dao.PlayerDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DATABASE_NAME).build()

    @Provides
    fun providePlayerDao(database: AppDatabase): PlayerDao = database.playerDao()

    @Provides
    fun provideEquipmentDao(database: AppDatabase): EquipmentDao = database.equipmentDao()
}
