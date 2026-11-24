package com.example.apex.database.dao

import com.example.apex.R
import com.example.apex.util.UiMessage
import com.example.apex.util.ResourceException
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.example.apex.data.EnumTypeActivite
import com.example.apex.data.Workout
import com.example.apex.database.entity.ExerciseEntity
import com.example.apex.database.entity.WorkoutEntity
import com.example.apex.database.entity.WorkoutExerciseEntity
import com.example.apex.database.relation.WorkoutAvecExercices
import com.example.apex.database.toEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class WorkoutDao {
    @Transaction
    @Query("SELECT * FROM workouts ORDER BY id DESC")
    abstract fun observerWorkouts(): Flow<List<WorkoutAvecExercices>>

    @Transaction
    @Query("SELECT * FROM workouts WHERE id = :id")
    abstract fun observerWorkout(id: Int): Flow<WorkoutAvecExercices?>

    @Transaction
    @Query("SELECT * FROM workouts WHERE id = :id")
    abstract suspend fun trouverWorkout(id: Int): WorkoutAvecExercices?

    @Insert abstract suspend fun insererWorkout(workout: WorkoutEntity): Long

    // Mise à jour des seuls champs du formulaire : les résultats du suivi GPS
    // (distance, durée) ne sont jamais écrasés par une modification de la séance.
    @Query("""
        UPDATE workouts SET titre = :titre, typeActivite = :typeActivite, lieu = :lieu,
            isOutside = :isOutside, intensite = :intensite, notes = :notes, done = :done,
            favori = :favori, datePrevue = :datePrevue, rappelActif = :rappelActif,
            rappelEnvoye = :rappelEnvoye
        WHERE id = :id
    """)
    abstract suspend fun modifierChampsFormulaire(
        id: Int, titre: String, typeActivite: EnumTypeActivite, lieu: String,
        isOutside: Boolean, intensite: Int, notes: String, done: Boolean, favori: Boolean,
        datePrevue: Long?, rappelActif: Boolean, rappelEnvoye: Boolean
    ): Int
    @Upsert abstract suspend fun sauvegarderExercice(exercice: ExerciseEntity)
    @Insert abstract suspend fun insererConfigurations(exercices: List<WorkoutExerciseEntity>)

    @Query("DELETE FROM workout_exercises WHERE workoutId = :id")
    abstract suspend fun supprimerConfigurations(id: Int)

    @Query("DELETE FROM workouts WHERE id = :id")
    abstract suspend fun supprimerWorkout(id: Int)

    @Query("UPDATE workouts SET favori = NOT favori WHERE id = :id")
    abstract suspend fun basculerFavori(id: Int)

    @Query("UPDATE workouts SET done = NOT done WHERE id = :id")
    abstract suspend fun basculerComplete(id: Int)

    @Query("UPDATE workouts SET done = 1 WHERE id = :id")
    abstract suspend fun marquerComplete(id: Int)

    /**
     * Séances à rappeler : rappel actif, non complétées, non notifiées, et qui débutent
     * dans l'intervalle ]debut, fin]. Une séance déjà commencée (heure passée) est exclue.
     */
    @Transaction
    @Query("""
        SELECT * FROM workouts
        WHERE rappelActif = 1 AND rappelEnvoye = 0 AND done = 0
            AND datePrevue IS NOT NULL AND datePrevue > :debut AND datePrevue <= :fin
        ORDER BY datePrevue
    """)
    abstract suspend fun trouverRappelsAEnvoyer(debut: Long, fin: Long): List<WorkoutAvecExercices>

    @Query("UPDATE workouts SET rappelEnvoye = 1 WHERE id = :id")
    abstract suspend fun marquerRappelEnvoye(id: Int)

    @Query("UPDATE workouts SET distanceMetres = :distanceMetres, dureeSecondes = :dureeSecondes WHERE id = :id")
    abstract suspend fun enregistrerSuivi(id: Int, distanceMetres: Double, dureeSecondes: Long)
    @Transaction
    open suspend fun enregistrer(workout: Workout) {
        require(workout.titre.trim().length >= 3 && workout.lieu.isNotBlank())
        require(workout.intensite in 1..10)
        require(workout.exercices.all {
            it.exercice.id.isNotBlank() && it.series > 0 && it.repetitions > 0 &&
                it.charge.isFinite() && it.charge >= 0
        })
        val id = if (workout.id == 0) insererWorkout(workout.toEntity()).toInt() else {
            val lignes = with(workout) {
                modifierChampsFormulaire(id, titre, typeActivite, lieu, isOutside, intensite,
                    notes, done, favori, datePrevue, rappelActif, rappelEnvoye)
            }
            if (lignes != 1) {
                throw ResourceException(UiMessage(R.string.erreur_seance_introuvable))
            }
            workout.id
        }
        supprimerConfigurations(id)
        val configurations = workout.exercices.mapIndexed { position, exercice ->
            sauvegarderExercice(exercice.exercice.toEntity())
            WorkoutExerciseEntity(workoutId = id, exerciseId = exercice.exercice.id,
                position = position, series = exercice.series,
                repetitions = exercice.repetitions, charge = exercice.charge)
        }
        insererConfigurations(configurations)
    }
}
