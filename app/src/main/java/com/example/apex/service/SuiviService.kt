package com.example.apex.service

import android.Manifest
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.example.apex.ApexApplication
import com.example.apex.notification.NotificationHelper
import com.example.apex.repository.SuiviTermine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Service au premier plan de type « location » qui suit une séance extérieure.
 * Démarré uniquement par une action de l'utilisateur (bouton « Démarrer le suivi »).
 * Publie la durée et la distance dans SuiviRepository ; ne connaît aucun écran.
 */
class SuiviService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val app get() = application as ApexApplication
    private lateinit var notifications: NotificationHelper

    private var suivi: Job? = null
    private var idWorkout: Int? = null
    private var debut = 0L
    private var distanceMetres = 0.0
    // Seule la dernière position est gardée, en mémoire, pour calculer le segment suivant.
    private var dernierePosition: Location? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        notifications = NotificationHelper(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_DEMARRER -> demarrer(intent.getIntExtra(EXTRA_ID_WORKOUT, 0))
            ACTION_ARRETER -> arreter()
            else -> stopSelf()
        }
        return START_NOT_STICKY
    }

    private fun demarrer(id: Int) {
        if (suivi != null) return // Un seul suivi actif à la fois.
        if (id == 0 || !permissionPrecise()) {
            app.suiviRepository.annuler()
            stopSelf()
            return
        }

        try {
            ServiceCompat.startForeground(
                this, NotificationHelper.ID_SUIVI,
                notifications.construireNotificationSuivi(id, 0, 0.0),
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
                } else 0
            )
        } catch (e: Exception) {
            // Démarrage au premier plan refusé par le système : on abandonne proprement.
            app.suiviRepository.annuler()
            stopSelf()
            return
        }

        idWorkout = id
        debut = SystemClock.elapsedRealtime()
        distanceMetres = 0.0
        dernierePosition = null
        app.suiviRepository.publier(id, 0, 0.0)

        suivi = scope.launch {
            launch {
                app.locationRepository.positions().collect { position -> ajouterPosition(position) }
            }
            while (isActive) {
                publier()
                delay(1_000)
            }
        }
    }

    private fun ajouterPosition(position: Location) {
        // Les positions trop imprécises font « sauter » le tracé et gonflent la distance.
        if (position.hasAccuracy() && position.accuracy > PRECISION_MAX_M) return
        dernierePosition?.let { distanceMetres += it.distanceTo(position) }
        dernierePosition = position
    }

    private fun dureeSecondes() = (SystemClock.elapsedRealtime() - debut) / 1000

    private fun publier() {
        val id = idWorkout ?: return
        val duree = dureeSecondes()
        app.suiviRepository.publier(id, duree, distanceMetres)
        notifications.mettreAJourSuivi(id, duree, distanceMetres)
    }

    private fun arreter() {
        val id = idWorkout
        // Annuler la collecte retire immédiatement les mises à jour de position (awaitClose).
        suivi?.cancel()
        suivi = null
        dernierePosition = null

        if (id != null) {
            val resultat = SuiviTermine(id, distanceMetres, dureeSecondes())
            idWorkout = null
            // Portée de l'application : l'enregistrement se termine même si le service est détruit.
            app.applicationScope.launch {
                app.repository.enregistrerSuivi(resultat.idWorkout, resultat.distanceMetres,
                    resultat.dureeSecondes)
                app.suiviRepository.terminer(resultat)
            }
        } else {
            app.suiviRepository.annuler()
        }
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        // Arrêt imposé par le système : on conserve ce qui a été mesuré.
        if (idWorkout != null) arreter()
        scope.cancel()
        super.onDestroy()
    }

    private fun permissionPrecise() = ContextCompat.checkSelfPermission(
        this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    companion object {
        private const val ACTION_DEMARRER = "com.example.apex.suivi.DEMARRER"
        private const val ACTION_ARRETER = "com.example.apex.suivi.ARRETER"
        private const val EXTRA_ID_WORKOUT = "idWorkout"
        private const val PRECISION_MAX_M = 30f

        fun intentDemarrer(context: Context, idWorkout: Int) =
            Intent(context, SuiviService::class.java)
                .setAction(ACTION_DEMARRER)
                .putExtra(EXTRA_ID_WORKOUT, idWorkout)

        fun intentArreter(context: Context) =
            Intent(context, SuiviService::class.java).setAction(ACTION_ARRETER)
    }
}
