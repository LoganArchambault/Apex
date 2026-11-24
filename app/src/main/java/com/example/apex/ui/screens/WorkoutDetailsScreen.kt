package com.example.apex.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.apex.R
import com.example.apex.data.EnumTypeActivite
import com.example.apex.data.Workout
import com.example.apex.repository.EtatSuivi
import com.example.apex.ui.components.LigneInterrupteur
import com.example.apex.ui.components.localeCourante
import com.example.apex.ui.components.texteDistance
import com.example.apex.ui.theme.ApexTheme
import com.example.apex.util.formaterDateHeure
import com.example.apex.util.formaterDuree
import com.example.apex.util.formaterNombre
import com.example.apex.util.libelleRes
import com.example.apex.viewmodel.WorkoutDetailsUiState
import com.example.apex.viewmodel.WorkoutDetailsViewModel
import com.example.apex.viewmodel.WorkoutViewModelFactory

/** Issue d'une demande de localisation qui n'a pas donné la position précise. */
enum class MessageLocalisation { APPROXIMATIVE, REFUSEE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutDetailsScreen(
    modifier: Modifier = Modifier,
    idWorkout: Int,
    onWorkoutChanged: () -> Unit = {},
    onBackClick: () -> Unit,
    onEditClick: () -> Unit = {},
    viewModel: WorkoutDetailsViewModel = viewModel(factory = WorkoutViewModelFactory.details(idWorkout))
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val contexte = LocalContext.current
    var afficherConfirmation by remember { mutableStateOf(false) }
    var expliquerLocalisation by rememberSaveable { mutableStateOf(false) }
    var messageLocalisation by rememberSaveable { mutableStateOf<MessageLocalisation?>(null) }

    // Position précise ET approximative demandées ensemble (exigence d'Android 12+), avec
    // POST_NOTIFICATIONS si nécessaire. Jamais ACCESS_BACKGROUND_LOCATION.
    val demandeLocalisation = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { resultats ->
        messageLocalisation = when {
            resultats[Manifest.permission.ACCESS_FINE_LOCATION] == true -> {
                // Le refus des notifications n'empêche pas le suivi.
                viewModel.demarrerSuivi()
                null
            }
            resultats[Manifest.permission.ACCESS_COARSE_LOCATION] == true -> MessageLocalisation.APPROXIMATIVE
            else -> MessageLocalisation.REFUSEE
        }
    }
    val lancerDemande = { demandeLocalisation.launch(permissionsSuivi(contexte)) }

    Scaffold(
        topBar = {
            WorkoutDetailsTopAppBar(
                onBackClick = onBackClick,
                onDeleteClick = { afficherConfirmation = true },
                onEditClick = onEditClick
            )
        }
    ) { innerPadding ->
        WorkoutDetailsContent(
            uiState = uiState,
            modifier = modifier.padding(innerPadding),
            onToggleComplete = { viewModel.toggleComplete(onWorkoutChanged) },
            onDemarrerSuivi = {
                if (permissionsSuivi(contexte).isEmpty()) {
                    viewModel.demarrerSuivi()
                } else {
                    expliquerLocalisation = true
                }
            },
            onArreterSuivi = viewModel::arreterSuivi,
            messageLocalisation = messageLocalisation,
            onReessayer = lancerDemande,
            onOuvrirParametres = { ouvrirParametresApplication(contexte) }
        )

        if (afficherConfirmation) {
            ConfirmDeleteDialog(
                onConfirm = {
                    afficherConfirmation = false
                    viewModel.supprimerWorkout {
                        onWorkoutChanged()
                        onBackClick()
                    }
                },
                onDismiss = { afficherConfirmation = false }
            )
        }

        if (expliquerLocalisation) {
            AlertDialog(
                onDismissRequest = { expliquerLocalisation = false },
                title = { Text(stringResource(R.string.permission_localisation_titre)) },
                text = { Text(stringResource(R.string.permission_localisation_explication)) },
                confirmButton = {
                    TextButton(onClick = {
                        expliquerLocalisation = false
                        lancerDemande()
                    }) { Text(stringResource(R.string.btn_continuer)) }
                },
                dismissButton = {
                    TextButton(onClick = { expliquerLocalisation = false }) {
                        Text(stringResource(R.string.btn_annuler))
                    }
                }
            )
        }

        if (uiState.proposerCompletion) {
            val workout = uiState.workout
            AlertDialog(
                onDismissRequest = { viewModel.repondreCompletion(false) },
                title = { Text(stringResource(R.string.suivi_termine_titre)) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (workout?.distanceMetres != null && workout.dureeSecondes != null) {
                            Text(stringResource(R.string.suivi_resultat,
                                texteDistance(workout.distanceMetres), formaterDuree(workout.dureeSecondes)))
                        }
                        Text(stringResource(R.string.suivi_termine_question))
                    }
                },
                confirmButton = {
                    TextButton(onClick = { viewModel.repondreCompletion(true) }) {
                        Text(stringResource(R.string.btn_marquer_complete))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.repondreCompletion(false) }) {
                        Text(stringResource(R.string.btn_plus_tard))
                    }
                }
            )
        }
    }
}

/** Permissions encore manquantes pour le suivi ; vide si la position précise est déjà accordée. */
private fun permissionsSuivi(contexte: Context): Array<String> {
    fun accordee(permission: String) =
        ContextCompat.checkSelfPermission(contexte, permission) == PackageManager.PERMISSION_GRANTED

    if (accordee(Manifest.permission.ACCESS_FINE_LOCATION)) return emptyArray()
    return buildList {
        add(Manifest.permission.ACCESS_FINE_LOCATION)
        add(Manifest.permission.ACCESS_COARSE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !accordee(Manifest.permission.POST_NOTIFICATIONS)
        ) add(Manifest.permission.POST_NOTIFICATIONS)
    }.toTypedArray()
}

private fun ouvrirParametresApplication(contexte: Context) {
    contexte.startActivity(
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", contexte.packageName, null))
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkoutDetailsTopAppBar(
    onBackClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit
) {
    TopAppBar(
        title = { Text(stringResource(R.string.detail_titre)) },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.cd_retour)
                )
            }
        },
        actions = {
            IconButton(onClick = onEditClick) {
                Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.form_modifier))
            }
            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.cd_supprimer)
                )
            }
        }
    )
}

@Composable
private fun WorkoutDetailsContent(
    uiState: WorkoutDetailsUiState,
    modifier: Modifier = Modifier,
    onToggleComplete: () -> Unit,
    onDemarrerSuivi: () -> Unit = {},
    onArreterSuivi: () -> Unit = {},
    messageLocalisation: MessageLocalisation? = null,
    onReessayer: () -> Unit = {},
    onOuvrirParametres: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.Start
    ) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator()
            }

            uiState.erreur != null && uiState.workout == null -> {
                Text(
                    text = uiState.erreur.asString(),
                    color = MaterialTheme.colorScheme.error
                )
            }

            uiState.workout == null -> {
                Text(
                    text = stringResource(R.string.detail_introuvable),
                    color = MaterialTheme.colorScheme.error
                )
            }

            else -> {
                uiState.erreur?.let { Text(it.asString(), color = MaterialTheme.colorScheme.error) }
                WorkoutDetailsCard(
                    workout = uiState.workout,
                    onToggleComplete = onToggleComplete
                )
                if (uiState.peutDemarrerSuivi || uiState.suiviDeCetteSeance) {
                    SectionSuivi(
                        uiState = uiState,
                        onDemarrer = onDemarrerSuivi,
                        onArreter = onArreterSuivi,
                        message = messageLocalisation,
                        onReessayer = onReessayer,
                        onOuvrirParametres = onOuvrirParametres
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionSuivi(
    uiState: WorkoutDetailsUiState,
    onDemarrer: () -> Unit,
    onArreter: () -> Unit,
    message: MessageLocalisation?,
    onReessayer: () -> Unit,
    onOuvrirParametres: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.suivi_titre), style = MaterialTheme.typography.titleMedium)
            when {
                uiState.suiviDeCetteSeance -> {
                    Text(stringResource(R.string.suivi_duree, formaterDuree(uiState.suivi.dureeSecondes)))
                    Text(stringResource(R.string.suivi_distance, texteDistance(uiState.suivi.distanceMetres)))
                    Button(onClick = onArreter) { Text(stringResource(R.string.suivi_arreter)) }
                }

                uiState.autreSuiviEnCours -> {
                    Text(stringResource(R.string.suivi_autre_en_cours))
                }

                else -> {
                    Button(onClick = onDemarrer) { Text(stringResource(R.string.suivi_demarrer)) }
                    when (message) {
                        MessageLocalisation.APPROXIMATIVE -> {
                            Text(stringResource(R.string.permission_localisation_approximative),
                                color = MaterialTheme.colorScheme.error)
                            OutlinedButton(onClick = onReessayer) { Text(stringResource(R.string.btn_reessayer)) }
                        }

                        MessageLocalisation.REFUSEE -> {
                            Text(stringResource(R.string.permission_localisation_refusee),
                                color = MaterialTheme.colorScheme.error)
                            OutlinedButton(onClick = onOuvrirParametres) {
                                Text(stringResource(R.string.btn_ouvrir_parametres))
                            }
                        }

                        null -> Unit
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkoutDetailsCard(
    workout: Workout,
    onToggleComplete: () -> Unit
) {
    val locale = localeCourante()
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = workout.titre,
            style = MaterialTheme.typography.headlineMedium
        )

        Text(stringResource(R.string.detail_type, stringResource(workout.typeActivite.libelleRes())))

        Text(
            stringResource(
                R.string.detail_lieu,
                workout.lieu,
                if (workout.isOutside) stringResource(R.string.txt_ext) else stringResource(R.string.txt_int)
            )
        )

        Text(
            workout.datePrevue?.let { stringResource(R.string.detail_date_prevue, formaterDateHeure(it, locale)) }
                ?: stringResource(R.string.detail_non_planifiee)
        )

        Text(
            stringResource(
                when {
                    !workout.rappelActif -> R.string.detail_rappel_inactif
                    workout.rappelEnvoye -> R.string.detail_rappel_envoye
                    else -> R.string.detail_rappel_actif
                }
            )
        )

        Text(stringResource(R.string.detail_intensite, workout.intensite))

        // Résultats du suivi GPS : affichés seulement après un suivi.
        if (workout.distanceMetres != null) {
            Text(stringResource(R.string.detail_distance, texteDistance(workout.distanceMetres)))
        }
        if (workout.dureeSecondes != null) {
            Text(stringResource(R.string.detail_duree, formaterDuree(workout.dureeSecondes)))
        }

        Text(stringResource(R.string.detail_notes), style = MaterialTheme.typography.titleMedium)

        Text(
            if (workout.notes.isBlank()) stringResource(R.string.detail_aucune_note) else workout.notes
        )

        if (workout.typeActivite == EnumTypeActivite.Musculation) {
            Text(stringResource(R.string.exercices_titre), style = MaterialTheme.typography.titleLarge)
            if (workout.exercices.isEmpty()) Text(stringResource(R.string.exercices_aucun))
            workout.exercices.forEach { configuration ->
                Text(configuration.exercice.nom, style = MaterialTheme.typography.titleMedium)
                Text(
                    stringResource(
                        R.string.exercice_configuration,
                        pluralStringResource(R.plurals.nb_series, configuration.series, configuration.series),
                        pluralStringResource(R.plurals.nb_repetitions, configuration.repetitions,
                            configuration.repetitions),
                        formaterNombre(configuration.charge, locale)
                    )
                )
                Text(listOf(configuration.exercice.muscleCible, configuration.exercice.equipement)
                    .filter { it.isNotBlank() }.joinToString(stringResource(R.string.separateur_caracteristiques)))
                if (configuration.exercice.instructions.isNotBlank()) Text(configuration.exercice.instructions)
                if (configuration.exercice.attribution.isNotBlank()) {
                    Text(configuration.exercice.attribution, style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        LigneInterrupteur(
            texte = stringResource(R.string.detail_completee),
            coche = workout.done,
            onChange = { onToggleComplete() }
        )
    }
}

@Composable
private fun ConfirmDeleteDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.detail_supprimer_titre)) },
        text = { Text(stringResource(R.string.detail_supprimer_message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.btn_supprimer))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_annuler))
            }
        }
    )
}

@Composable
private fun workoutApercu() = Workout(
    id = 1,
    titre = stringResource(R.string.preview_course_matin),
    typeActivite = EnumTypeActivite.Course,
    lieu = stringResource(R.string.preview_parc),
    isOutside = true,
    intensite = 7,
    notes = stringResource(R.string.preview_course_notes),
    done = false,
    favori = true,
    datePrevue = 1_790_000_000_000L,
    rappelActif = true,
    distanceMetres = 5_230.0,
    dureeSecondes = 1_745
)

@Preview(name = "Mode clair", uiMode = UI_MODE_NIGHT_NO, showBackground = true)
@Preview(name = "Mode sombre", uiMode = UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun WorkoutDetailsPreview() {
    ApexTheme {
        WorkoutDetailsContent(
            uiState = WorkoutDetailsUiState(workout = workoutApercu()),
            onToggleComplete = {}
        )
    }
}

@Preview(name = "Suivi en cours", showBackground = true)
@Preview(name = "Suivi en cours, police agrandie", fontScale = 2f, showBackground = true, heightDp = 1400)
@Composable
fun WorkoutDetailsSuiviPreview() {
    ApexTheme {
        WorkoutDetailsContent(
            uiState = WorkoutDetailsUiState(
                workout = workoutApercu(),
                suivi = EtatSuivi(idWorkout = 1, dureeSecondes = 754, distanceMetres = 2_140.0)
            ),
            onToggleComplete = {}
        )
    }
}

@Preview(name = "Position approximative", showBackground = true)
@Composable
fun WorkoutDetailsApproximativePreview() {
    ApexTheme {
        WorkoutDetailsContent(
            uiState = WorkoutDetailsUiState(workout = workoutApercu()),
            onToggleComplete = {},
            messageLocalisation = MessageLocalisation.APPROXIMATIVE
        )
    }
}
