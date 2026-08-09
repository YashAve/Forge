package com.development.forge.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.development.forge.data.local.dao.SubtaskDao
import com.development.forge.data.local.dao.TaskDao
import com.development.forge.data.local.entity.SubTaskEntity
import com.development.forge.data.local.entity.TaskEntity

@Database(entities = [TaskEntity::class, SubTaskEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun subtaskDao(): SubtaskDao
}