package com.nvv.mediadata.di

import android.content.Context
import androidx.room.Room
import com.nvv.mediadata.data.db.AppDatabase
import com.nvv.mediadata.data.db.HistoryDao
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
	fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
		return Room.databaseBuilder(
			context,
			AppDatabase::class.java,
			"mediadata_db"
		).build()
	}

	@Provides
	fun provideHistoryDao(database: AppDatabase): HistoryDao {
		return database.historyDao()
	}
}
