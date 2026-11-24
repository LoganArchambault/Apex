package com.example.apex.viewmodel

import androidx.annotation.StringRes

import com.example.apex.util.UiMessage
import com.example.apex.data.EnumTypeActivite
import com.example.apex.data.Exercise
import com.example.apex.data.WorkoutExercise

data class CreateWorkoutUiState(
    val id: Int = 0,
    val done: Boolean = false,
    val favori: Boolean = false,
    val isLoading: Boolean = false,
    val chargementImpossible: Boolean = false,
    val exercices: List<ExerciceFormState> = emptyList(),
    val rechercheOuverte: Boolean = false,
    val motCle: String = "",
    val rechercheEnCours: Boolean = false,
    val rechercheEffectuee: Boolean = false,
    val resultats: List<Exercise> = emptyList(),
    val erreurRecherche: UiMessage? = null,
    val titre: String = "",
    val typeActivite: EnumTypeActivite = EnumTypeActivite.Course,
    val lieu: String = "",
    val isOutside: Boolean = false,
    val intensite: Int = 5,
    val notes: String = "",
    /** Instant prévu en millisecondes UTC (null : non planifiée). */
    val datePrevue: Long? = null,
    val rappelActif: Boolean = false,
    val rappelEnvoye: Boolean = false,
    /** Erreur de la règle « rappel actif → date obligatoire et future ». */
    @param:StringRes val erreurDate: Int? = null,
    val erreurTitre: Boolean = false,
    val erreurLieu: Boolean = false,
    val isSaving: Boolean = false,
    val erreur: UiMessage? = null
)
data class ExerciceFormState(
    val exercice: Exercise,
    val series: String = "3",
    val repetitions: String = "10",
    val charge: String = "0"
) {
    fun versModele(): WorkoutExercise? {
        val nbSeries = series.toIntOrNull()?.takeIf { it > 0 } ?: return null
        val nbRepetitions = repetitions.toIntOrNull()?.takeIf { it > 0 } ?: return null
        val poids = charge.replace(',', '.').toDoubleOrNull()
            ?.takeIf { it.isFinite() && it >= 0 } ?: return null
        return WorkoutExercise(exercice, nbSeries, nbRepetitions, poids)
    }
}
