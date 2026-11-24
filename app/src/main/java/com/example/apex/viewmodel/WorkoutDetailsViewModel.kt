package com.example.apex.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apex.R
import com.example.apex.repository.IWorkoutRepository
import com.example.apex.repository.SuiviRepository
import com.example.apex.util.toUiMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WorkoutDetailsViewModel(
    private val repository: IWorkoutRepository,
    private val suiviRepository: SuiviRepository,
    private val idWorkout: Int
) : ViewModel() {
    private val _uiState = MutableStateFlow(WorkoutDetailsUiState(isLoading = true))
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                repository.observerWorkout(idWorkout).collect { workout ->
                    _uiState.update { it.copy(workout = workout, isLoading = false) }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, erreur = e.toUiMessage(R.string.erreur_generique)) }
            }
        }
        // Valeurs en direct publiées par le service, via l'objet partagé.
        viewModelScope.launch {
            suiviRepository.etat.collect { etat -> _uiState.update { it.copy(suivi = etat) } }
        }
        viewModelScope.launch {
            suiviRepository.termine.collect { termine ->
                _uiState.update { it.copy(proposerCompletion = termine?.idWorkout == idWorkout) }
            }
        }
    }

    fun toggleComplete(onSuccess: () -> Unit = {}) = effectuer {
        repository.basculerComplete(idWorkout)
        onSuccess()
    }

    fun supprimerWorkout(onSuccess: () -> Unit) = effectuer {
        if (_uiState.value.suiviDeCetteSeance) suiviRepository.arreter()
        repository.supprimerWorkout(idWorkout)
        onSuccess()
    }

    /** Appelé seulement après l'obtention de la position précise. */
    fun demarrerSuivi() {
        if (_uiState.value.peutDemarrerSuivi) suiviRepository.demarrer(idWorkout)
    }

    fun arreterSuivi() = suiviRepository.arreter()

    fun repondreCompletion(marquerComplete: Boolean) {
        suiviRepository.acquitterFin()
        if (marquerComplete) effectuer { repository.marquerComplete(idWorkout) }
    }

    private fun effectuer(action: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(erreur = null) }
                action()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(erreur = e.toUiMessage(R.string.erreur_operation)) }
            }
        }
    }
}
