package com.example.apex.repository

import com.example.apex.data.EnumTypeActivite
import com.example.apex.data.Workout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.milliseconds

class FakeWorkoutRepository : IWorkoutRepository {

    override suspend fun chargerWorkouts(): List<Workout> = withContext(Dispatchers.IO) {
        delay(300.milliseconds)
        FakeWorkoutData.fullList.toList()
    }

    override suspend fun sauvegarderWorkout(workout: Workout) = withContext(Dispatchers.IO) {
        val index = FakeWorkoutData.fullList.indexOfFirst { it.id == workout.id }

        if (index == -1) {
            throw Exception("Entraînement introuvable")
        }

        FakeWorkoutData.fullList[index] = workout
        delay(300.milliseconds)
    }

    override suspend fun trouverWorkout(id: Int): Workout? = withContext(Dispatchers.IO) {
        delay(500.milliseconds)
        FakeWorkoutData.fullList.firstOrNull { it.id == id }
    }

    override suspend fun ajouterWorkout(workout: Workout) {
        withContext(Dispatchers.IO) {
            delay(500.milliseconds)

            val nouvelId = (FakeWorkoutData.fullList.maxOfOrNull { it.id } ?: 0) + 1

            FakeWorkoutData.fullList.add(
                workout.copy(id = nouvelId)
            )
        }
    }

    override suspend fun supprimerWorkout(id: Int) = withContext(Dispatchers.IO) {
        val workout = FakeWorkoutData.fullList.firstOrNull { it.id == id }

        if (workout != null) {
            FakeWorkoutData.fullList.remove(workout)
        } else {
            throw Exception("Entraînement introuvable")
        }

        delay(300.milliseconds)
    }
}

object FakeWorkoutData {
    val fullList = mutableListOf(
        Workout(
            id = 1,
            titre = "Séance de course",
            typeActivite = EnumTypeActivite.Course,
            lieu = "Parc",
            isOutside = true,
            intensite = 7,
            notes = "Bonne séance",
            done = false,
            favori = false
        ),
        Workout(
            id = 2,
            titre = "Séance de natation",
            typeActivite = EnumTypeActivite.Natation,
            lieu = "Piscine",
            isOutside = false,
            intensite = 8,
            notes = "Excellente séance",
            done = true,
            favori = true
        ),
        Workout(
            id = 3,
            titre = "Séance de vélo",
            typeActivite = EnumTypeActivite.Velo,
            lieu = "Montagne",
            isOutside = true,
            intensite = 6,
            notes = "Séance difficile mais agréable",
            done = false,
            favori = true
        ),
        Workout(
            id = 4,
            titre = "Séance de musculation",
            typeActivite = EnumTypeActivite.Musculation,
            lieu = "Salle de sport",
            isOutside = false,
            intensite = 9,
            notes = "Séance intense mais satisfaisante",
            done = true,
            favori = false
        )
    )
}
