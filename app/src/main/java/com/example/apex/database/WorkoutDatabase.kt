package com.example.apex.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.apex.database.dao.WorkoutDao
import com.example.apex.database.entity.ExerciseEntity
import com.example.apex.database.entity.WorkoutEntity
import com.example.apex.database.entity.WorkoutExerciseEntity

@Database(entities = [WorkoutEntity::class, ExerciseEntity::class,
    WorkoutExerciseEntity::class], version = 2, exportSchema = true)
@TypeConverters(WorkoutConverters::class)
abstract class WorkoutDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao

    companion object {
        const val NOM = "apex_database.db"

        @Volatile private var INSTANCE: WorkoutDatabase? = null
        fun getInstance(context: Context): WorkoutDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(context.applicationContext, WorkoutDatabase::class.java, NOM)
                // Migration explicite : aucune destruction de données (pas de fallbackToDestructiveMigration).
                .addMigrations(MIGRATION_1_2)
                .build().also { INSTANCE = it }
        }
    }
}
