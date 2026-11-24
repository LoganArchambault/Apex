package com.example.apex.repository

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Paramètres de l'application, conservés localement sur l'appareil. */
class PreferencesRepository(context: Context) {
    private val preferences = context.applicationContext
        .getSharedPreferences(NOM_FICHIER, Context.MODE_PRIVATE)

    private val _verrouillageActif = MutableStateFlow(preferences.getBoolean(CLE_VERROUILLAGE, false))
    val verrouillageActif: StateFlow<Boolean> = _verrouillageActif.asStateFlow()

    fun modifierVerrouillage(actif: Boolean) {
        preferences.edit { putBoolean(CLE_VERROUILLAGE, actif) }
        _verrouillageActif.value = actif
    }

    private companion object {
        const val NOM_FICHIER = "apex_parametres"
        const val CLE_VERROUILLAGE = "verrouillage_actif"
    }
}
