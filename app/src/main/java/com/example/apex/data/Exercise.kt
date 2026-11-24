package com.example.apex.data

data class Exercise(
    val id: String,
    val nom: String,
    val categorie: String = "",
    val equipement: String = "",
    val muscleCible: String = "",
    val instructions: String = "",
    val langue: String = "fr",
    val attribution: String = ""
)

data class WorkoutExercise(
    val exercice: Exercise,
    val series: Int = 3,
    val repetitions: Int = 10,
    val charge: Double = 0.0
)
