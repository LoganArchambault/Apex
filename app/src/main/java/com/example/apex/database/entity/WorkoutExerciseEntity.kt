package com.example.apex.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// Les séries, répétitions et charges appartiennent à l'association, pas au catalogue.
@Entity(
    tableName = "workout_exercises",
    foreignKeys = [
        ForeignKey(entity = WorkoutEntity::class, parentColumns = ["id"],
            childColumns = ["workoutId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = ExerciseEntity::class, parentColumns = ["id"],
            childColumns = ["exerciseId"], onDelete = ForeignKey.RESTRICT)
    ],
    indices = [Index("workoutId"), Index("exerciseId")]
)
data class WorkoutExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val workoutId: Int,
    val exerciseId: String,
    val position: Int,
    val series: Int,
    val repetitions: Int,
    val charge: Double
)
