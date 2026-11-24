package com.example.apex.viewmodel

import com.example.apex.R
import com.example.apex.util.toUiMessage
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apex.data.EnumTypeActivite
import com.example.apex.data.Workout
import com.example.apex.repository.IWorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException

class WorkoutViewModel(
    private val repository: IWorkoutRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkoutUiState())
    val uiState: StateFlow<WorkoutUiState> = _uiState.asStateFlow()

    private var observation: Job? = null

    init {
        chargerWorkouts()
    }

    fun chargerWorkouts() {
        observation?.cancel()
        observation = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                repository.observerWorkouts().collect { workouts ->
                    _uiState.update {
                        appliquerFiltres(it.copy(workouts = workouts, isLoading = false))
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.toUiMessage(R.string.erreur_generique)) }
            }
        }
    }

    fun modifierRecherche(texte: String) {
        _uiState.update {
            appliquerFiltres(it.copy(recherche = texte))
        }
    }

    fun modifierFiltreFavoris(active: Boolean) {
        _uiState.update {
            appliquerFiltres(it.copy(favorisSeulement = active))
        }
    }

    fun modifierType(type: EnumTypeActivite?) {
        _uiState.update {
            appliquerFiltres(it.copy(typeSelectionne = type))
        }
    }

    fun toggleFavori(workout: Workout) {
        viewModelScope.launch {
            try {
                repository.basculerFavori(workout.id)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = e.toUiMessage(R.string.erreur_generique))
                }
            }
        }
    }

    /** Suggestion aléatoire parmi les séances non complétées (secousse ou « Surprenez-moi »). */
    fun suggererSeance() {
        _uiState.update { etat ->
            etat.copy(afficherSuggestion = true,
                suggestion = etat.workouts.filter { !it.done }.randomOrNull())
        }
    }

    fun fermerSuggestion() = _uiState.update { it.copy(afficherSuggestion = false, suggestion = null) }

    private fun appliquerFiltres(etat: WorkoutUiState): WorkoutUiState {
        val workoutsFiltres = etat.workouts.filter { workout ->
            val correspondRecherche = etat.recherche.isBlank() ||
                workout.titre.contains(etat.recherche.trim(), ignoreCase = true)

            val correspondFavori = !etat.favorisSeulement || workout.favori
            val correspondType = etat.typeSelectionne == null ||
                workout.typeActivite == etat.typeSelectionne

            correspondRecherche && correspondFavori && correspondType
        }

        val completed = etat.workouts.count { it.done }
        val total = etat.workouts.size

        return etat.copy(
            filteredWorkouts = workoutsFiltres,
            completedCount = completed,
            totalCount = total,
            completionProgress = if (total == 0) 0f else completed.toFloat() / total,
            nextWorkout = etat.workouts.firstOrNull { !it.done }
        )
    }
}
