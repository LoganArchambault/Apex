package com.example.apex.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.apex.ApexApplication
import com.example.apex.notification.NotificationHelper
import kotlinx.coroutines.CancellationException

/**
 * Vérification périodique des rappels.
 * Retient les séances avec rappel actif, non complétées, non notifiées, qui débutent dans
 * l'heure qui suit, puis notifie chacune et le consigne dans Room (jamais deux fois).
 *
 * Aucune référence à une Activity ni à un écran : seulement le contexte d'application.
 */
class RappelWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val notifications = NotificationHelper(applicationContext)
        // Notifications désactivées : on ne fait rien et on ne marque rien comme envoyé,
        // le rappel pourra partir à une exécution ultérieure si l'utilisateur les réactive.
        if (!notifications.peutNotifier()) return Result.success()

        return try {
            val repository = (applicationContext as ApexApplication).repository
            val maintenant = System.currentTimeMillis()
            val seances = repository.trouverRappelsAEnvoyer(maintenant, maintenant + FENETRE_MS)

            seances.forEach { seance ->
                if (notifications.afficherRappel(seance)) {
                    repository.marquerRappelEnvoye(seance.id)
                }
            }
            Result.success()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Journalisation sans donnée personnelle ; on réessaiera plus tard.
            Log.e(TAG, "Vérification des rappels impossible", e)
            if (runAttemptCount < MAX_ESSAIS) Result.retry() else Result.failure()
        }
    }

    companion object {
        private const val TAG = "RappelWorker"
        private const val MAX_ESSAIS = 3
        /** Une séance est rappelée si elle débute dans l'heure qui suit. */
        const val FENETRE_MS = 60 * 60 * 1000L
    }
}
