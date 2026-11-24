package com.example.apex

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.apex.data.EnumTypeActivite
import com.example.apex.data.Exercise
import com.example.apex.data.Workout
import com.example.apex.data.WorkoutExercise
import com.example.apex.database.WorkoutDatabase
import com.example.apex.repository.RoomWorkoutRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WorkoutDatabaseTest {
    private lateinit var db: WorkoutDatabase
    private lateinit var repository: RoomWorkoutRepository
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val nomDb = "apex-test.db"
    @Before fun preparer() {
        context.deleteDatabase(nomDb)
        ouvrir()
    }
    private fun ouvrir() {
        db = Room.databaseBuilder(context, WorkoutDatabase::class.java, nomDb).build()
        repository = RoomWorkoutRepository(db.workoutDao())
    }
    @After fun fermer() { db.close(); context.deleteDatabase(nomDb) }
    private fun seance(titre: String, charge: Double) = Workout(0, titre,
        EnumTypeActivite.Musculation, "Gym", false, 5, "", false, false,
        listOf(WorkoutExercise(Exercise("0025", "Bench press", instructions = "Poussez."),
            series = 3, repetitions = 10, charge = charge)))

    @Test fun persisteApresReouvertureEtSepareLesConfigurations() = runBlocking {
        repository.ajouterWorkout(seance("Séance A", 20.0))
        repository.ajouterWorkout(seance("Séance B", 40.0))
        db.close()
        ouvrir()
        val seances = repository.observerWorkouts().first()
        assertEquals(2, seances.size)
        assertEquals(setOf(20.0, 40.0), seances.map { it.exercices.single().charge }.toSet())
        val a = seances.first { it.titre == "Séance A" }
        repository.sauvegarderWorkout(a.copy(exercices = listOf(a.exercices.single().copy(series = 4))))
        assertEquals(4, repository.trouverWorkout(a.id)!!.exercices.single().series)
        assertEquals(3, repository.trouverWorkout(seances.first { it.id != a.id }.id)!!.exercices.single().series)
        repository.supprimerWorkout(a.id)
        assertNull(repository.trouverWorkout(a.id))
        assertEquals(1, repository.observerWorkouts().first().size)
    }

    @Test fun flowNotifieEtSuppressionExerciceConserveLaSeance() = runBlocking {
        repository.ajouterWorkout(seance("Séance test", 0.0))
        val seance = repository.observerWorkouts().first().single()
        val notification = async {
            withTimeout(5000) { repository.observerWorkout(seance.id).first { it?.favori == true } }
        }
        repository.basculerFavori(seance.id)
        assertTrue(notification.await()!!.favori)
        repository.sauvegarderWorkout(repository.trouverWorkout(seance.id)!!.copy(exercices = emptyList()))
        assertTrue(repository.trouverWorkout(seance.id)!!.exercices.isEmpty())
        repository.basculerComplete(seance.id)
        assertTrue(repository.trouverWorkout(seance.id)!!.done)
    }

    @Test fun selectionDesRappelsEtAucunDoublon() = runBlocking {
        val maintenant = System.currentTimeMillis()
        val heure = 60 * 60 * 1000L
        fun planifiee(titre: String, date: Long, actif: Boolean = true, done: Boolean = false) =
            seance(titre, 0.0).copy(datePrevue = date, rappelActif = actif, done = done)

        repository.ajouterWorkout(planifiee("Dans 30 min", maintenant + heure / 2))
        repository.ajouterWorkout(planifiee("Dans 2 h", maintenant + 2 * heure))
        repository.ajouterWorkout(planifiee("Déjà passée", maintenant - 60_000))
        repository.ajouterWorkout(planifiee("Sans rappel", maintenant + heure / 2, actif = false))
        repository.ajouterWorkout(planifiee("Complétée", maintenant + heure / 2, done = true))

        val aRappeler = repository.trouverRappelsAEnvoyer(maintenant, maintenant + heure)
        assertEquals(listOf("Dans 30 min"), aRappeler.map { it.titre })

        repository.marquerRappelEnvoye(aRappeler.single().id)
        assertTrue(repository.trouverRappelsAEnvoyer(maintenant, maintenant + heure).isEmpty())
    }

    @Test fun suiviEnregistreEtConserveApresModification() = runBlocking {
        repository.ajouterWorkout(seance("Sortie vélo", 0.0))
        val id = repository.observerWorkouts().first().single().id
        repository.enregistrerSuivi(id, 12_345.0, 2_400)
        // Modifier la séance ne doit pas effacer le résultat du suivi.
        repository.sauvegarderWorkout(repository.trouverWorkout(id)!!.copy(titre = "Sortie vélo longue"))
        val seance = repository.trouverWorkout(id)!!
        assertEquals(12_345.0, seance.distanceMetres!!, 0.0)
        assertEquals(2_400L, seance.dureeSecondes)
        assertEquals("Sortie vélo longue", seance.titre)
    }
}
