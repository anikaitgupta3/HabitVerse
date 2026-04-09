package com.example.habitverse.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.habitverse.data.db.Habit
import com.example.habitverse.data.db.HabitDao

@Database(entities = [Habit::class, HabitLog::class], version = 3, exportSchema = false)
@TypeConverters(Converters::class)
abstract class HabitDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao

    /*companion object {
        @Volatile
        private var Instance: HabitDatabase? = null

        fun getDatabase(context: Context): HabitDatabase {
            // if the Instance is not null, return it, otherwise create a new database instance.
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, HabitDatabase::class.java, "habit_database")
                    .fallbackToDestructiveMigration(true)
                    .build()
                    .also { Instance = it }
            }
        }
    }*/
}