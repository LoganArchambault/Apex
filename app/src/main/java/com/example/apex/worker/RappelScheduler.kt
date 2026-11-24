package com.example.apex.worker

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/** Planification des vérifications de rappels avec WorkManager. */
object RappelScheduler {
    private const val TRAVAIL_PERIODIQUE = "verification_rappels"
    private const val TRAVAIL_PONCTUEL = "verification_rappels_immediate"

    /**
     * Tâche périodique unique (KEEP : un seul exemplaire, jamais dupliqué à chaque démarrage).
     * WorkManager la conserve après la fermeture de l'application et le redémarrage de l'appareil.
     * L'heure exacte n'est pas garantie : seule l'exécution l'est.
     */
    fun planifier(context: Context) {
        val requete = PeriodicWorkRequestBuilder<RappelWorker>(1, TimeUnit.HOURS).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            TRAVAIL_PERIODIQUE, ExistingPeriodicWorkPolicy.KEEP, requete)
    }

    /** Même Worker en tâche ponctuelle (utile pour tester sans attendre une heure). */
    fun verifierMaintenant(context: Context) {
        WorkManager.getInstance(context).enqueueUniqueWork(
            TRAVAIL_PONCTUEL, ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<RappelWorker>().build())
    }
}
