package com.example.apex.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apex.data.EnumTypeActivite
import com.example.apex.data.Workout
import com.example.apex.repository.FakeWorkoutRepository
import com.example.apex.repository.IWorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WorkoutViewModel(
    private val repository: IWorkoutRepository = FakeWorkoutRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkoutUiState())
    val uiState: StateFlow<WorkoutUiState> = _uiState.asStateFlow()

    init {
        chargerWorkouts()
    }

    fun chargerWorkouts() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true, errorMessage = null)
            }

            try {
                val workouts = repository.chargerWorkouts()

                _uiState.update { etatActuel ->
                    val nouvelEtat = etatActuel.copy(
                        workouts = workouts,
                        isLoading = false,
                        errorMessage = null
                    )
                    appliquerFiltres(nouvelEtat)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message
                    )
                }
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
                repository.sauvegarderWorkout(
                    workout.copy(favori = !workout.favori)
                )
                chargerWorkouts()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = e.message)
                }
            }
        }
    }

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
