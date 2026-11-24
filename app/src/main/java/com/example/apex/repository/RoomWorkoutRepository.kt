package com.example.apex.repository

import com.example.apex.data.Workout
import com.example.apex.database.dao.WorkoutDao
import com.example.apex.database.toModel
import kotlinx.coroutines.flow.map

class RoomWorkoutRepository(private val workoutDao: WorkoutDao) : IWorkoutRepository {
    override fun observerWorkouts() = workoutDao.observerWorkouts().map { liste ->
        liste.map { it.toModel() }
    }
    override fun observerWorkout(id: Int) = workoutDao.observerWorkout(id).map { it?.toModel() }
    override suspend fun trouverWorkout(id: Int) = workoutDao.trouverWorkout(id)?.toModel()
    override suspend fun sauvegarderWorkout(workout: Workout) = workoutDao.enregistrer(workout)
    override suspend fun ajouterWorkout(workout: Workout) = workoutDao.enregistrer(workout.copy(id = 0))
    override suspend fun supprimerWorkout(id: Int) = workoutDao.supprimerWorkout(id)
    override suspend fun basculerFavori(id: Int) = workoutDao.basculerFavori(id)
    override suspend fun basculerComplete(id: Int) = workoutDao.basculerComplete(id)
    override suspend fun marquerComplete(id: Int) = workoutDao.marquerComplete(id)
    override suspend fun trouverRappelsAEnvoyer(debut: Long, fin: Long) =
        workoutDao.trouverRappelsAEnvoyer(debut, fin).map { it.toModel() }
    override suspend fun marquerRappelEnvoye(id: Int) = workoutDao.marquerRappelEnvoye(id)
    override suspend fun enregistrerSuivi(id: Int, distanceMetres: Double, dureeSecondes: Long) =
        workoutDao.enregistrerSuivi(id, distanceMetres, dureeSecondes)
}
