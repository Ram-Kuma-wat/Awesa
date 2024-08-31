package com.game.awesa.di

import android.content.Context
import android.database.sqlite.SQLiteOpenHelper
import com.codersworld.awesalibs.database.DatabaseHelper
import com.codersworld.awesalibs.database.DatabaseManager
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module()
class DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(context: Context): DatabaseManager {
        val databaseHelper = DatabaseHelper(context)
        return DatabaseManager(databaseHelper)
    }
}
