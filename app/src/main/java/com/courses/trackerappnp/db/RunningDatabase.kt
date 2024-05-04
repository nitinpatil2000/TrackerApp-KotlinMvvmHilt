package com.courses.trackerappnp.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.courses.trackerappnp.db.Converters
import com.courses.trackerappnp.db.Run
import com.courses.trackerappnp.db.RunDao

@Database(entities = [Run::class], version = 1)
@TypeConverters(Converters::class)
abstract class RunningDatabase : RoomDatabase() {

    abstract fun provideRunDao(): RunDao
}