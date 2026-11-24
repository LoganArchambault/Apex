package com.example.apex.data

/**
 * Séance d'entraînement.
 *
 * @property datePrevue Instant prévu, en millisecondes depuis l'époque Unix (UTC). Le fuseau
 *   horaire de l'appareil n'est appliqué qu'à l'affichage.
 * @property rappelActif L'utilisateur souhaite recevoir un rappel avant la séance.
 * @property rappelEnvoye Le rappel a déjà été notifié (évite tout doublon).
 * @property distanceMetres Distance mesurée par le suivi GPS, `null` tant qu'aucun suivi n'a eu lieu.
 * @property dureeSecondes Durée réelle mesurée par le suivi GPS, `null` tant qu'aucun suivi n'a eu lieu.
 */
data class Workout(
    val id: Int,
    val titre: String,
    val typeActivite: EnumTypeActivite,
    val lieu: String,
    val isOutside: Boolean,
    val intensite: Int,
    val notes: String,
    val done: Boolean,
    val favori: Boolean,
    val exercices: List<WorkoutExercise> = emptyList(),
    val datePrevue: Long? = null,
    val rappelActif: Boolean = false,
    val rappelEnvoye: Boolean = false,
    val distanceMetres: Double? = null,
    val dureeSecondes: Long? = null
)
