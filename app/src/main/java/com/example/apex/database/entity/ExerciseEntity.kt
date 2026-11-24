package com.example.apex.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey val id: String,
    val nom: String,
    val categorie: String,
    val equipement: String,
    val muscleCible: String,
    val instructions: String,
    val langue: String,
    val attribution: String
)
