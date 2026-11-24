package com.example.apex.repository

import android.content.Context
import androidx.core.content.ContextCompat
import com.example.apex.service.SuiviService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** État en direct du suivi GPS. */
data class EtatSuivi(
    val idWorkout: Int? = null,
    val dureeSecondes: Long = 0,
    val distanceMetres: Double = 0.0
) {
    val enCours: Boolean get() = idWorkout != null
}

/** Résultat d'un suivi terminé, en attente de la question « marquer comme complétée ? ». */
data class SuiviTermine(val idWorkout: Int, val distanceMetres: Double, val dureeSecondes: Long)

/**
 * Objet partagé entre SuiviService (qui publie) et les ViewModels (qui observent).
 * L'interface ne parle jamais au service : elle passe par son ViewModel, qui passe par ici.
 * Vit dans ApexApplication : une rotation d'écran ne touche ni au suivi ni à son état.
 */
class SuiviRepository(context: Context) {
    private val context = context.applicationContext

    private val _etat = MutableStateFlow(EtatSuivi())
    val etat: StateFlow<EtatSuivi> = _etat.asStateFlow()

    private val _termine = MutableStateFlow<SuiviTermine?>(null)
    val termine: StateFlow<SuiviTermine?> = _termine.asStateFlow()

    /** Démarre le suivi (action de l'utilisateur). Un seul suivi peut être actif à la fois. */
    fun demarrer(idWorkout: Int): Boolean {
        if (_etat.value.enCours) return false
        _etat.value = EtatSuivi(idWorkout = idWorkout)
        ContextCompat.startForegroundService(context, SuiviService.intentDemarrer(context, idWorkout))
        return true
    }

    fun arreter() {
        if (_etat.value.enCours) context.startService(SuiviService.intentArreter(context))
    }

    fun acquitterFin() {
        _termine.value = null
    }

    // --- Appelées uniquement par SuiviService ---

    internal fun publier(idWorkout: Int, dureeSecondes: Long, distanceMetres: Double) {
        _etat.value = EtatSuivi(idWorkout, dureeSecondes, distanceMetres)
    }

    internal fun terminer(resultat: SuiviTermine) {
        _etat.value = EtatSuivi()
        _termine.value = resultat
    }

    internal fun annuler() {
        _etat.value = EtatSuivi()
    }
}
