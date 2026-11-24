package com.example.apex.viewmodel

import com.example.apex.R
import com.example.apex.util.toUiMessage
import com.example.apex.util.ResourceException
import com.example.apex.util.UiMessage
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apex.data.EnumTypeActivite
import com.example.apex.data.Exercise
import com.example.apex.data.Workout
import com.example.apex.repository.IExerciseRepository
import com.example.apex.repository.IWorkoutRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.annotation.StringRes
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset

class CreateWorkoutViewModel(
    private val repository: IWorkoutRepository,
    private val exerciseRepository: IExerciseRepository,
    private val idWorkout: Int? = null
) : ViewModel() {
    private val _uiState = MutableStateFlow(CreateWorkoutUiState(isLoading = idWorkout != null))
    val uiState = _uiState.asStateFlow()
    private var recherche: Job? = null

    init {
        // Validation en temps réel : une date future finit par passer pendant la saisie.
        viewModelScope.launch {
            while (true) {
                delay(REVALIDATION_MS)
                _uiState.update { valider(it) }
            }
        }
        if (idWorkout != null) viewModelScope.launch {
            try {
                val workout = repository.trouverWorkout(idWorkout)
                    ?: throw ResourceException(UiMessage(R.string.erreur_seance_introuvable))
                _uiState.value = CreateWorkoutUiState(
                    id = workout.id, titre = workout.titre, typeActivite = workout.typeActivite,
                    lieu = workout.lieu, isOutside = workout.isOutside, intensite = workout.intensite,
                    notes = workout.notes, done = workout.done, favori = workout.favori,
                    datePrevue = workout.datePrevue, rappelActif = workout.rappelActif,
                    rappelEnvoye = workout.rappelEnvoye,
                    exercices = workout.exercices.map {
                        ExerciceFormState(it.exercice, it.series.toString(),
                            it.repetitions.toString(), it.charge.toString())
                    })
                _uiState.update { valider(it) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, chargementImpossible = true,
                    erreur = e.toUiMessage(R.string.erreur_generique)) }
            }
        }
    }

    fun modifierTitre(titre: String) = _uiState.update {
        it.copy(titre = titre, erreurTitre = titre.trim().length < 3)
    }
    fun modifierLieu(lieu: String) = _uiState.update {
        it.copy(lieu = lieu, erreurLieu = lieu.isBlank())
    }
    fun modifierTypeActivite(type: EnumTypeActivite) {
        fermerRecherche()
        _uiState.update { it.copy(typeActivite = type) }
    }
    fun modifierIsOutside(outside: Boolean) = _uiState.update { it.copy(isOutside = outside) }
    fun modifierIntensite(intensite: Int) = _uiState.update { it.copy(intensite = intensite.coerceIn(1, 10)) }
    fun modifierNotes(notes: String) = _uiState.update { it.copy(notes = notes) }

    /**
     * @param dateUtc date choisie dans le DatePicker (minuit UTC de ce jour). L'heure déjà
     * choisie est conservée ; sinon l'heure suivante est proposée.
     */
    fun modifierDate(dateUtc: Long) = _uiState.update { etat ->
        val jour = Instant.ofEpochMilli(dateUtc).atZone(ZoneOffset.UTC).toLocalDate()
        val heure = etat.datePrevue?.let { versLocal(it).toLocalTime() } ?: heureParDefaut()
        changerDate(etat, versMillis(LocalDateTime.of(jour, heure)))
    }

    fun modifierHeure(heure: Int, minute: Int) = _uiState.update { etat ->
        val jour = etat.datePrevue?.let { versLocal(it).toLocalDate() } ?: LocalDate.now()
        changerDate(etat, versMillis(LocalDateTime.of(jour, LocalTime.of(heure, minute))))
    }

    fun effacerDate() = _uiState.update { changerDate(it, null) }

    fun modifierRappel(actif: Boolean) = _uiState.update { etat ->
        // Un rappel réactivé est considéré comme n'ayant pas encore été envoyé.
        val envoye = if (actif && !etat.rappelActif) false else etat.rappelEnvoye
        valider(etat.copy(rappelActif = actif, rappelEnvoye = envoye))
    }

    // Une nouvelle date ou heure remet le rappel à « non envoyé ».
    private fun changerDate(etat: CreateWorkoutUiState, date: Long?) =
        valider(etat.copy(datePrevue = date, rappelEnvoye = false))

    private fun valider(etat: CreateWorkoutUiState) = etat.copy(erreurDate = erreurDate(etat))

    /** Règle : si le rappel est actif, la date et l'heure sont obligatoires et dans le futur. */
    @StringRes
    private fun erreurDate(etat: CreateWorkoutUiState, maintenant: Long = System.currentTimeMillis()): Int? =
        when {
            !etat.rappelActif -> null
            etat.datePrevue == null -> R.string.date_obligatoire_rappel
            etat.datePrevue <= maintenant -> R.string.date_dans_le_futur
            else -> null
        }

    private fun versLocal(millis: Long) = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault())
    private fun versMillis(date: LocalDateTime) = date.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    private fun heureParDefaut() = LocalTime.now().plusHours(1).withMinute(0).withSecond(0).withNano(0)
    fun ouvrirRecherche() = _uiState.update { it.copy(rechercheOuverte = true) }
    fun fermerRecherche() {
        recherche?.cancel()
        _uiState.update { it.copy(rechercheOuverte = false, rechercheEnCours = false) }
    }
    fun modifierMotCle(motCle: String) {
        recherche?.cancel()
        _uiState.update { it.copy(motCle = motCle, rechercheEnCours = false,
            rechercheEffectuee = false, erreurRecherche = null, resultats = emptyList()) }
    }
    fun chercherExercices() {
        val motCle = _uiState.value.motCle.trim()
        if (motCle.isBlank()) return
        recherche?.cancel()
        recherche = viewModelScope.launch {
            _uiState.update { it.copy(rechercheEnCours = true, erreurRecherche = null,
                resultats = emptyList(), rechercheEffectuee = false) }
            try {
                val resultats = exerciseRepository.chercherExercices(motCle)
                _uiState.update { it.copy(rechercheEnCours = false, rechercheEffectuee = true,
                    resultats = resultats) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(rechercheEnCours = false, erreurRecherche = e.toUiMessage(R.string.erreur_recherche)) }
            }
        }
    }
    fun ajouterExercice(exercice: Exercise) {
        fermerRecherche()
        _uiState.update { it.copy(exercices = it.exercices + ExerciceFormState(exercice)) }
    }
    fun supprimerExercice(index: Int) = _uiState.update {
        it.copy(exercices = it.exercices.filterIndexed { position, _ -> position != index })
    }
    fun modifierExercice(index: Int, series: String, repetitions: String, charge: String) = _uiState.update {
        it.copy(exercices = it.exercices.mapIndexed { position, exercice ->
            if (position == index) exercice.copy(series = series, repetitions = repetitions,
                charge = charge) else exercice
        })
    }

    fun estValide(): Boolean = with(_uiState.value) {
        !isLoading && !chargementImpossible && titre.trim().length >= 3 && lieu.isNotBlank() &&
            erreurDate(this) == null &&
            (typeActivite != EnumTypeActivite.Musculation || exercices.all { it.versModele() != null })
    }

    fun creerWorkout(onSuccess: () -> Unit) {
        if (!estValide()) {
            // La date a pu passer depuis la dernière saisie : on affiche l'erreur à jour.
            _uiState.update { valider(it) }
            return
        }
        if (_uiState.value.isSaving) return
        val state = _uiState.value
        _uiState.update { it.copy(isSaving = true, erreur = null) }
        viewModelScope.launch {
            try {
                val workout = Workout(id = state.id, titre = state.titre.trim(),
                    typeActivite = state.typeActivite, lieu = state.lieu.trim(),
                    isOutside = state.isOutside, intensite = state.intensite,
                    notes = state.notes.trim(), done = state.done, favori = state.favori,
                    datePrevue = state.datePrevue, rappelActif = state.rappelActif,
                    rappelEnvoye = state.rappelActif && state.rappelEnvoye,
                    exercices = if (state.typeActivite == EnumTypeActivite.Musculation)
                        state.exercices.map { checkNotNull(it.versModele()) } else emptyList())
                if (idWorkout == null) repository.ajouterWorkout(workout)
                else repository.sauvegarderWorkout(workout)
                _uiState.update { it.copy(isSaving = false) }
                onSuccess()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, erreur = e.toUiMessage(R.string.erreur_sauvegarde)) }
            }
        }
    }

    private companion object {
        const val REVALIDATION_MS = 15_000L
    }
}
