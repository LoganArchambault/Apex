package com.example.apex.security

import android.os.SystemClock
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * État de verrouillage de l'application, observé sur le cycle de vie du processus
 * (ProcessLifecycleOwner). Un singleton survit aux rotations : celles-ci ne redemandent
 * donc jamais l'authentification.
 */
object LockManager : DefaultLifecycleObserver {
    private const val DELAI_INACTIVITE_MS = 30_000L

    private var actif: () -> Boolean = { false }
    private var tempsArrierePlan = 0L

    private val _verrouille = MutableStateFlow(false)
    val verrouille: StateFlow<Boolean> = _verrouille.asStateFlow()

    /** @param verrouillageActif lecture du paramètre « Verrouiller l'application ». */
    fun initialiser(verrouillageActif: () -> Boolean) {
        actif = verrouillageActif
        _verrouille.value = verrouillageActif()
    }

    /** Lancement de l'application (nouvelle activité, pas une rotation). */
    fun verrouillerSiActif() {
        if (actif()) _verrouille.value = true
    }

    override fun onStop(owner: LifecycleOwner) {
        tempsArrierePlan = SystemClock.elapsedRealtime()
    }

    override fun onStart(owner: LifecycleOwner) {
        val absence = SystemClock.elapsedRealtime() - tempsArrierePlan
        if (tempsArrierePlan > 0L && absence > DELAI_INACTIVITE_MS) verrouillerSiActif()
    }

    fun deverrouiller() {
        _verrouille.value = false
    }
}
