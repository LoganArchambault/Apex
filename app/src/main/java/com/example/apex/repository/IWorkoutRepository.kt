package com.example.apex.repository

import com.example.apex.data.Workout
import kotlinx.coroutines.flow.Flow

interface IWorkoutRepository {
    fun observerWorkouts(): Flow<List<Workout>>
    fun observerWorkout(id: Int): Flow<Workout?>
    suspend fun trouverWorkout(id: Int): Workout?
    suspend fun sauvegarderWorkout(workout: Workout)
    suspend fun ajouterWorkout(workout: Workout)
    suspend fun supprimerWorkout(id: Int)
    suspend fun basculerFavori(id: Int)
    suspend fun basculerComplete(id: Int)
    suspend fun marquerComplete(id: Int)

    /** Séances avec rappel actif, non complétées, non notifiées, prévues dans ]debut, fin]. */
    suspend fun trouverRappelsAEnvoyer(debut: Long, fin: Long): List<Workout>
    suspend fun marquerRappelEnvoye(id: Int)

    /** Enregistre le résultat d'un suivi GPS (aucune coordonnée, seulement distance et durée). */
    suspend fun enregistrerSuivi(id: Int, distanceMetres: Double, dureeSecondes: Long)
}
