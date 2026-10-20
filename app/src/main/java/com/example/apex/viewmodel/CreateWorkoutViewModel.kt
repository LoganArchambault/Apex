package com.example.apex.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apex.data.EnumTypeActivite
import com.example.apex.data.Workout
import com.example.apex.repository.FakeWorkoutRepository
import com.example.apex.repository.IWorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CreateWorkoutViewModel(
    private val repository: IWorkoutRepository = FakeWorkoutRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateWorkoutUiState())
    val uiState = _uiState.asStateFlow()

    fun modifierTitre(titre: String) {
        _uiState.update {
            it.copy(
                titre = titre,
                erreurTitre = titre.trim().length < 3
            )
        }
    }

    fun modifierTypeActivite(typeActivite: EnumTypeActivite) {
        _uiState.update {
            it.copy(typeActivite = typeActivite)
        }
    }

    fun modifierLieu(lieu: String) {
        _uiState.update {
            it.copy(
                lieu = lieu,
                erreurLieu = lieu.isBlank()
            )
        }
    }

    fun modifierIsOutside(isOutside: Boolean) {
        _uiState.update {
            it.copy(isOutside = isOutside)
        }
    }

    fun modifierIntensite(intensite: Int) {
        _uiState.update {
            it.copy(intensite = intensite.coerceIn(1, 10))
        }
    }

    fun modifierNotes(notes: String) {
        _uiState.update {
            it.copy(notes = notes)
        }
    }

    fun estValide(): Boolean {
        val state = _uiState.value
        return state.titre.trim().length >= 3 && state.lieu.isNotBlank()
    }

    fun creerWorkout(onSuccess: () -> Unit) {
        val state = _uiState.value

        if (!estValide()) {
            _uiState.update {
                it.copy(
                    erreurTitre = state.titre.trim().length < 3,
                    erreurLieu = state.lieu.isBlank()
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(isSaving = true, erreur = null)
            }

            try {
                repository.ajouterWorkout(
                    Workout(
                        id = 0,
                        titre = state.titre.trim(),
                        typeActivite = state.typeActivite,
                        lieu = state.lieu.trim(),
                        isOutside = state.isOutside,
                        intensite = state.intensite,
                        notes = state.notes.trim(),
                        done = false,
                        favori = false
                    )
                )

                _uiState.update { it.copy(isSaving = false) }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        erreur = e.message
                    )
                }
            }
        }
    }
}
