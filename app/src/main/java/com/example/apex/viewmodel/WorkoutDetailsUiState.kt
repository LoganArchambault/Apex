package com.example.apex.viewmodel

import com.example.apex.data.Workout
import com.example.apex.repository.EtatSuivi
import com.example.apex.util.UiMessage

data class WorkoutDetailsUiState(
    val workout: Workout? = null,
    val isLoading: Boolean = false,
    val erreur: UiMessage? = null,
    /** État en direct du suivi GPS (toutes séances confondues). */
    val suivi: EtatSuivi = EtatSuivi(),
    /** Le suivi de cette séance vient de se terminer : proposer de la marquer complétée. */
    val proposerCompletion: Boolean = false
) {
    val suiviDeCetteSeance: Boolean get() = suivi.enCours && suivi.idWorkout == workout?.id
    val autreSuiviEnCours: Boolean get() = suivi.enCours && suivi.idWorkout != workout?.id
    val peutDemarrerSuivi: Boolean
        get() = workout != null && workout.isOutside && !workout.done
}
