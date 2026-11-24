package com.example.apex.database

import com.example.apex.data.Exercise
import com.example.apex.data.Workout
import com.example.apex.data.WorkoutExercise
import com.example.apex.database.entity.ExerciseEntity
import com.example.apex.database.entity.WorkoutEntity
import com.example.apex.database.relation.WorkoutAvecExercices

fun Workout.toEntity() = WorkoutEntity(
    id = id, titre = titre, typeActivite = typeActivite, lieu = lieu, isOutside = isOutside,
    intensite = intensite, notes = notes, done = done, favori = favori,
    datePrevue = datePrevue, rappelActif = rappelActif, rappelEnvoye = rappelEnvoye,
    distanceMetres = distanceMetres, dureeSecondes = dureeSecondes
)

fun Exercise.toEntity() = ExerciseEntity(id, nom, categorie, equipement, muscleCible,
    instructions, langue, attribution)

fun ExerciseEntity.toModel() = Exercise(id, nom, categorie, equipement, muscleCible,
    instructions, langue, attribution)

fun WorkoutAvecExercices.toModel() = with(workout) {
    Workout(
        id = id, titre = titre, typeActivite = typeActivite, lieu = lieu, isOutside = isOutside,
        intensite = intensite, notes = notes, done = done, favori = favori,
        exercices = exercices.sortedBy { it.configuration.position }.map {
            WorkoutExercise(it.exercice.toModel(), it.configuration.series,
                it.configuration.repetitions, it.configuration.charge)
        },
        datePrevue = datePrevue, rappelActif = rappelActif, rappelEnvoye = rappelEnvoye,
        distanceMetres = distanceMetres, dureeSecondes = dureeSecondes
    )
}
