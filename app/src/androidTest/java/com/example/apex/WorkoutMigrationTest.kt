package com.example.apex

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.apex.database.MIGRATION_1_2
import com.example.apex.database.WorkoutDatabase
import com.example.apex.repository.RoomWorkoutRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** La migration 1 → 2 conserve les séances existantes et respecte le schéma exporté. */
@RunWith(AndroidJUnit4::class)
class WorkoutMigrationTest {
    private val nomDb = "apex-migration-test.db"

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        WorkoutDatabase::class.java
    )

    @Test
    fun migration1a2ConserveLesDonnees() {
        helper.createDatabase(nomDb, 1).apply {
            execSQL("""
                INSERT INTO workouts (id, titre, typeActivite, lieu, isOutside, intensite, notes, done, favori)
                VALUES (7, 'Course du matin', 'Course', 'Parc', 1, 6, 'Tranquille', 0, 1)
            """.trimIndent())
            close()
        }

        // Valide aussi le schéma obtenu contre 2.json.
        helper.runMigrationsAndValidate(nomDb, 2, true, MIGRATION_1_2).close()

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val db = Room.databaseBuilder(context, WorkoutDatabase::class.java, nomDb)
            .addMigrations(MIGRATION_1_2).build()
        try {
            val seance = runBlocking { RoomWorkoutRepository(db.workoutDao()).trouverWorkout(7) }!!
            assertEquals("Course du matin", seance.titre)
            assertEquals("Parc", seance.lieu)
            assertEquals(true, seance.favori)
            assertNull(seance.datePrevue)
            assertFalse(seance.rappelActif)
            assertFalse(seance.rappelEnvoye)
            assertNull(seance.distanceMetres)
            assertNull(seance.dureeSecondes)
        } finally {
            db.close()
            context.deleteDatabase(nomDb)
        }
    }
}
