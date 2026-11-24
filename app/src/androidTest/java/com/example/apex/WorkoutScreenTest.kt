package com.example.apex

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.core.app.ApplicationProvider
import com.example.apex.data.EnumTypeActivite
import com.example.apex.data.Exercise
import com.example.apex.data.Workout
import com.example.apex.data.WorkoutExercise
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class WorkoutScreenTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun modifierUneChargeLocaleDepuisLesDetails() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<ApexApplication>()
        val repository = app.repository
        repository.observerWorkouts().first().forEach { repository.supprimerWorkout(it.id) }
        repository.ajouterWorkout(Workout(0, "Séance test interface", EnumTypeActivite.Musculation,
            "Gym", false, 5, "Test hors ligne", false, false,
            listOf(WorkoutExercise(Exercise("0025", "Bench press", instructions = "Poussez la barre."),
                3, 10, 20.0))))
        compose.waitUntil(10000) {
            compose.onAllNodesWithText("Séance test interface").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("Séance test interface").performClick()
        compose.onNodeWithText("Bench press").assertExists()
        compose.onNodeWithContentDescription("Modifier la séance").performClick()
        compose.waitUntil(10000) {
            compose.onAllNodes(hasText("Charge (kg)") and hasSetTextAction()).fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNode(hasText("Charge (kg)") and hasSetTextAction()).performScrollTo()
            .performTextReplacement("32,5")
        compose.onNodeWithText("Enregistrer").performScrollTo().performClick()
        compose.waitUntil(10000) {
            compose.onAllNodesWithText("3 séries × 10 répétitions • 32,5 kg").fetchSemanticsNodes().isNotEmpty()
        }
        val enregistre = repository.observerWorkouts().first().single()
        assertEquals(32.5, enregistre.exercices.single().charge, 0.0)
        repository.supprimerWorkout(enregistre.id)
    }
}
