package com.example.apex.repository

import com.example.apex.data.Workout

interface IWorkoutRepository {
    suspend fun chargerWorkouts(): List<Workout>
    suspend fun sauvegarderWorkout(workout: Workout)
    suspend fun trouverWorkout(id: Int): Workout?
    suspend fun supprimerWorkout(id: Int)
    suspend fun ajouterWorkout(workout: Workout)
}
