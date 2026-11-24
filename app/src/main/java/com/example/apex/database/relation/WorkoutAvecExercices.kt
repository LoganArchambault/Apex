package com.example.apex.database.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.apex.database.entity.ExerciseEntity
import com.example.apex.database.entity.WorkoutEntity
import com.example.apex.database.entity.WorkoutExerciseEntity

data class ExerciceConfigure(
    @Embedded val configuration: WorkoutExerciseEntity,
    @Relation(parentColumn = "exerciseId", entityColumn = "id")
    val exercice: ExerciseEntity
)

data class WorkoutAvecExercices(
    @Embedded val workout: WorkoutEntity,
    @Relation(parentColumn = "id", entityColumn = "workoutId",
        entity = WorkoutExerciseEntity::class)
    val exercices: List<ExerciceConfigure>
)
