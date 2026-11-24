package com.example.apex.viewmodel

import androidx.lifecycle.ViewModel
import com.example.apex.repository.PreferencesRepository
import kotlinx.coroutines.flow.StateFlow

class ParametresViewModel(
    private val preferences: PreferencesRepository
) : ViewModel() {
    val verrouillageActif: StateFlow<Boolean> = preferences.verrouillageActif

    /** L'activation n'est appelée qu'après une authentification réussie (voir l'écran). */
    fun modifierVerrouillage(actif: Boolean) = preferences.modifierVerrouillage(actif)
}
