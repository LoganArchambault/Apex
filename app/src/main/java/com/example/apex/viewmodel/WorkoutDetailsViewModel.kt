package com.example.apex.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apex.repository.FakeWorkoutRepository
import com.example.apex.repository.IWorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WorkoutDetailsViewModel(
    private val repository: IWorkoutRepository = FakeWorkoutRepository(),
    private val idWorkout: Int
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkoutDetailsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        chargerWorkout()
    }

    private fun chargerWorkout() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, erreur = null) }

            try {
                val workout = repository.trouverWorkout(idWorkout)

                _uiState.update {
                    it.copy(
                        workout = workout,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        erreur = e.message
                    )
                }
            }
        }
    }

    fun toggleComplete(onSuccess: () -> Unit = {}) {
        val workoutActuel = _uiState.value.workout ?: return
        val workoutModifie = workoutActuel.copy(done = !workoutActuel.done)

        _uiState.update {
            it.copy(workout = workoutModifie)
        }

        viewModelScope.launch {
            try {
                repository.sauvegarderWorkout(workoutModifie)
                onSuccess()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        workout = workoutActuel,
                        erreur = e.message
                    )
                }
            }
        }
    }

    fun supprimerWorkout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                repository.supprimerWorkout(idWorkout)
                onSuccess()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(erreur = e.message)
                }
            }
        }
    }
}
