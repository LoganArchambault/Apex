package com.example.apex.repository

import android.annotation.SuppressLint
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Positions fournies par le Fused Location Provider.
 * Vie privée : les positions ne sont jamais journalisées, conservées ni transmises ; elles ne
 * servent qu'au calcul de la distance, en mémoire, pendant le suivi.
 */
class FusedLocationRepository(
    private val client: FusedLocationProviderClient
) {
    /**
     * Priorité PRIORITY_HIGH_ACCURACY : mesurer la distance d'une course ou d'une sortie de vélo
     * exige le GPS (précision de quelques mètres). La précision « équilibrée » (~100 m, Wi-Fi et
     * réseau cellulaire) rendrait la distance cumulée inutilisable.
     *
     * Intervalle de 3 s (minimum 1 s) : à 10 km/h on parcourt ~8 m en 3 s, ce qui suit fidèlement
     * le trajet et ses virages, tout en limitant la consommation de batterie. Le coût reste
     * acceptable puisque les mises à jour ne sont actives que pendant la séance suivie.
     *
     * Les mises à jour cessent dès que le Flow n'est plus collecté (awaitClose).
     */
    @SuppressLint("MissingPermission") // Vérifiée par SuiviService avant toute collecte.
    fun positions(): Flow<Location> = callbackFlow {
        val requete = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, INTERVALLE_MS)
            .setMinUpdateIntervalMillis(INTERVALLE_MIN_MS)
            .setMinUpdateDistanceMeters(DISTANCE_MIN_M)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.locations.forEach { trySend(it) }
            }
        }

        client.requestLocationUpdates(requete, callback, Looper.getMainLooper())
        awaitClose { client.removeLocationUpdates(callback) }
    }

    private companion object {
        const val INTERVALLE_MS = 3_000L
        const val INTERVALLE_MIN_MS = 1_000L
        const val DISTANCE_MIN_M = 2f
    }
}
