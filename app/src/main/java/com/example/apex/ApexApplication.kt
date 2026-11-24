package com.example.apex

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.apex.database.WorkoutDatabase
import com.example.apex.network.RetrofitInstance
import com.example.apex.notification.NotificationHelper
import com.example.apex.repository.FusedLocationRepository
import com.example.apex.repository.MoveBaseRepository
import com.example.apex.repository.PreferencesRepository
import com.example.apex.repository.RoomWorkoutRepository
import com.example.apex.repository.SuiviRepository
import com.example.apex.security.LockManager
import com.example.apex.worker.RappelScheduler
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class ApexApplication : Application() {
    /** Portée qui vit aussi longtemps que le processus (ex. : enregistrement de fin de suivi). */
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val database by lazy { WorkoutDatabase.getInstance(this) }
    val repository by lazy { RoomWorkoutRepository(database.workoutDao()) }
    val exerciseRepository by lazy { MoveBaseRepository(RetrofitInstance.api, BuildConfig.MOVEBASE_API_KEY) }
    val preferencesRepository by lazy { PreferencesRepository(this) }
    val suiviRepository by lazy { SuiviRepository(this) }
    val locationRepository by lazy {
        FusedLocationRepository(LocationServices.getFusedLocationProviderClient(this))
    }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper(this).creerCanaux()

        LockManager.initialiser { preferencesRepository.verrouillageActif.value }
        ProcessLifecycleOwner.get().lifecycle.addObserver(LockManager)

        // Tâche périodique unique planifiée au démarrage (KEEP : jamais dupliquée).
        RappelScheduler.planifier(this)
        // Tests : le même Worker en tâche ponctuelle, pour ne pas attendre une heure.
        if (BuildConfig.DEBUG) RappelScheduler.verifierMaintenant(this)
    }
}
