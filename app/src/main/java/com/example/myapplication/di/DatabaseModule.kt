package com.example.myapplication.di

import android.content.Context
import com.example.myapplication.data.local.dao.TaskDao
import com.example.myapplication.data.local.database.AppDatabase
import com.example.myapplication.data.local.preferences.RoutinePreferences
import com.example.myapplication.data.local.preferences.WakeUpPreferences
import com.example.myapplication.data.repository.RoutineRepository
import com.example.myapplication.data.repository.TaskRepository
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
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideTaskDao(database: AppDatabase): TaskDao {
        return database.taskDao()
    }

    @Provides
    @Singleton
    fun provideTaskRepository(taskDao: TaskDao): TaskRepository {
        return TaskRepository(taskDao)
    }

    @Provides
    @Singleton
    fun provideWakeUpPreferences(@ApplicationContext context: Context): WakeUpPreferences {
        return WakeUpPreferences(context)
    }

    @Provides
    @Singleton
    fun provideRoutinePreferences(@ApplicationContext context: Context): RoutinePreferences {
        return RoutinePreferences(context)
    }

    @Provides
    @Singleton
    fun provideRoutineRepository(routinePreferences: RoutinePreferences): RoutineRepository {
        return RoutineRepository(routinePreferences)
    }
}
