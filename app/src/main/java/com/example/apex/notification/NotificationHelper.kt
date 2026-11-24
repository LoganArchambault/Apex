package com.example.apex.notification

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.apex.MainActivity
import com.example.apex.R
import com.example.apex.data.Workout
import com.example.apex.service.SuiviService
import com.example.apex.util.distanceLocalisee
import com.example.apex.util.formaterDateHeure
import com.example.apex.util.formaterDuree
import com.example.apex.util.libelleRes
import java.util.Locale

/**
 * Point central de la création des canaux et des notifications de l'application.
 * Les textes sont construits au moment de l'affichage avec getString(...) afin de suivre
 * la langue courante de l'application.
 *
 * Ne reçoit qu'un Context d'application ou de service : jamais une Activity.
 */
class NotificationHelper(context: Context) {

    private val context = context.applicationContext
    private val manager = NotificationManagerCompat.from(this.context)

    /** (Re)crée les canaux ; rappelé au démarrage pour mettre à jour leur nom si la langue change. */
    fun creerCanaux() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val systeme = context.getSystemService(NotificationManager::class.java)
        systeme.createNotificationChannel(
            NotificationChannel(CANAL_RAPPELS, context.getString(R.string.canal_rappels_nom),
                NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = context.getString(R.string.canal_rappels_description)
            }
        )
        systeme.createNotificationChannel(
            // Importance basse : la notification de suivi est mise à jour chaque seconde, sans son.
            NotificationChannel(CANAL_SUIVI, context.getString(R.string.canal_suivi_nom),
                NotificationManager.IMPORTANCE_LOW).apply {
                description = context.getString(R.string.canal_suivi_description)
            }
        )
    }

    /** Vrai si l'utilisateur permet l'affichage des notifications (permission + réglages système). */
    fun peutNotifier(): Boolean {
        val permission = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        return permission && manager.areNotificationsEnabled()
    }

    /**
     * Affiche le rappel d'une séance.
     * @return `true` seulement si la notification a réellement été publiée.
     */
    fun afficherRappel(workout: Workout): Boolean {
        if (!peutNotifier()) return false
        val locale = Locale.getDefault()
        val heure = workout.datePrevue?.let { formaterDateHeure(it, locale) } ?: return false
        val texte = context.getString(R.string.notif_rappel_texte,
            context.getString(workout.typeActivite.libelleRes()), workout.lieu, heure)

        val notification = NotificationCompat.Builder(context, CANAL_RAPPELS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(context.getString(R.string.notif_rappel_titre, workout.titre))
            .setContentText(texte)
            .setStyle(NotificationCompat.BigTextStyle().bigText(texte))
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(intentOuvrirSeance(workout.id))
            .setAutoCancel(true)
            .build()

        return try {
            manager.notify(ID_RAPPEL_BASE + workout.id, notification)
            true
        } catch (e: SecurityException) {
            // Permission retirée entre la vérification et l'envoi : le rappel n'est pas marqué envoyé.
            false
        }
    }

    /** Notification persistante du suivi GPS (durée, distance et bouton « Arrêter »). */
    fun construireNotificationSuivi(idWorkout: Int, dureeSecondes: Long, distanceMetres: Double): Notification {
        val distance = distanceLocalisee(distanceMetres, Locale.getDefault())
        val texte = context.getString(R.string.notif_suivi_texte, formaterDuree(dureeSecondes),
            context.getString(distance.unite, distance.valeur))

        val arreter = PendingIntent.getService(
            context, REQUETE_ARRETER_SUIVI, SuiviService.intentArreter(context),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, CANAL_SUIVI)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(context.getString(R.string.notif_suivi_titre))
            .setContentText(texte)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setCategory(NotificationCompat.CATEGORY_WORKOUT)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setContentIntent(intentOuvrirSeance(idWorkout))
            .addAction(0, context.getString(R.string.notif_suivi_arreter), arreter)
            .build()
    }

    fun mettreAJourSuivi(idWorkout: Int, dureeSecondes: Long, distanceMetres: Double) {
        if (!peutNotifier()) return
        try {
            manager.notify(ID_SUIVI, construireNotificationSuivi(idWorkout, dureeSecondes, distanceMetres))
        } catch (_: SecurityException) {
            // Notifications refusées : le suivi continue sans mise à jour visuelle.
        }
    }

    /** Ouvre l'application sur la fiche détaillée de la séance. */
    private fun intentOuvrirSeance(idWorkout: Int): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra(MainActivity.EXTRA_ID_WORKOUT, idWorkout)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        return PendingIntent.getActivity(context, idWorkout, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    companion object {
        const val CANAL_RAPPELS = "rappels"
        const val CANAL_SUIVI = "suivi"
        const val ID_SUIVI = 1001
        private const val ID_RAPPEL_BASE = 2000
        private const val REQUETE_ARRETER_SUIVI = 3000
    }
}
