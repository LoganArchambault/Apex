package com.example.apex.sensors

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.SystemClock
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlin.math.sqrt

/** Valeurs brutes d'un capteur ; l'écouteur est désinscrit dès que la collecte s'arrête. */
fun SensorManager.valeurs(
    capteur: Sensor,
    delai: Int = SensorManager.SENSOR_DELAY_GAME
): Flow<FloatArray> = callbackFlow {
    val ecouteur = object : SensorEventListener {
        override fun onSensorChanged(evenement: SensorEvent) {
            trySend(evenement.values.clone())
        }

        override fun onAccuracyChanged(capteur: Sensor, precision: Int) = Unit
    }

    registerListener(ecouteur, capteur, delai)
    awaitClose { unregisterListener(ecouteur) }
}

fun SensorManager.possedeAccelerometre(): Boolean =
    getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null

/**
 * Émet un événement par secousse.
 * @param seuilG force minimale (en g) ; au repos la force mesurée vaut ~1 g.
 * @param delaiMs anti-répétition : une seule secousse retenue par intervalle.
 * Sans accéléromètre, le Flow se termine sans rien émettre (aucune erreur).
 */
fun SensorManager.secousses(
    delai: Int = SensorManager.SENSOR_DELAY_GAME,
    seuilG: Float = 2.5f,
    delaiMs: Long = 1_500L
): Flow<Unit> = flow {
    val accelerometre = getDefaultSensor(Sensor.TYPE_ACCELEROMETER) ?: return@flow
    var derniere = 0L

    valeurs(accelerometre, delai).collect { v ->
        val force = sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]) / SensorManager.GRAVITY_EARTH
        val maintenant = SystemClock.elapsedRealtime()
        if (force > seuilG && maintenant - derniere > delaiMs) {
            derniere = maintenant
            emit(Unit)
        }
    }
}
