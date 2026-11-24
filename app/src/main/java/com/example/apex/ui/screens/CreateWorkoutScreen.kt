package com.example.apex.ui.screens

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.apex.R
import com.example.apex.data.EnumTypeActivite
import com.example.apex.viewmodel.CreateWorkoutUiState
import com.example.apex.viewmodel.CreateWorkoutViewModel
import kotlin.math.roundToInt
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.ui.tooling.preview.Preview
import com.example.apex.ui.theme.ApexTheme
import com.example.apex.viewmodel.WorkoutViewModelFactory
import com.example.apex.ui.components.ExerciseEditor
import com.example.apex.ui.components.ExerciseSearchDialog
import androidx.compose.material3.CircularProgressIndicator
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.text.format.DateFormat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.core.content.ContextCompat
import com.example.apex.ui.components.LigneInterrupteur
import com.example.apex.ui.components.localeCourante
import com.example.apex.util.formaterDateHeure
import com.example.apex.util.libelleRes
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateWorkoutScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    onWorkoutCreated: () -> Unit,
    idWorkout: Int? = null,
    viewModel: CreateWorkoutViewModel = viewModel(factory = WorkoutViewModelFactory.creation(idWorkout))
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var menuOuvert by remember { mutableStateOf(false) }
    var choisirDate by rememberSaveable { mutableStateOf(false) }
    var choisirHeure by rememberSaveable { mutableStateOf(false) }
    var expliquerNotifications by rememberSaveable { mutableStateOf(false) }
    var notificationsRefusees by rememberSaveable { mutableStateOf(false) }
    val contexte = LocalContext.current

    val demandeNotifications = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { accordee -> notificationsRefusees = !accordee }

    // POST_NOTIFICATIONS est demandée au moment où l'utilisateur active un rappel (jamais au
    // démarrage), et seulement après une explication.
    val onRappelChange: (Boolean) -> Unit = { actif ->
        viewModel.modifierRappel(actif)
        if (actif && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(contexte, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            expliquerNotifications = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (idWorkout == null) stringResource(R.string.form_titre_ecran) else stringResource(R.string.form_modifier)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_retour)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            CircularProgressIndicator(Modifier.padding(innerPadding).padding(24.dp))
            return@Scaffold
        }
        CreateWorkoutContent(
            uiState = uiState,
            modifier = modifier.padding(innerPadding),
            menuOuvert = menuOuvert,
            onMenuChange = { menuOuvert = it },
            onTitreChange = viewModel::modifierTitre,
            onTypeChange = { type ->
                viewModel.modifierTypeActivite(type)
                menuOuvert = false
            },
            onLieuChange = viewModel::modifierLieu,
            onIsOutsideChange = viewModel::modifierIsOutside,
            onIntensiteChange = viewModel::modifierIntensite,
            onNotesChange = viewModel::modifierNotes,
            onSaveClick = {
                viewModel.creerWorkout(onWorkoutCreated)
            },
            onChoisirDate = { choisirDate = true },
            onChoisirHeure = { choisirHeure = true },
            onEffacerDate = viewModel::effacerDate,
            onRappelChange = onRappelChange,
            notificationsRefusees = notificationsRefusees,
            formulaireValide = viewModel.estValide(),
            onAddExercise = viewModel::ouvrirRecherche,
            onExerciseChange = viewModel::modifierExercice,
            onDeleteExercise = viewModel::supprimerExercice
        )
        if (uiState.rechercheOuverte) {
            ExerciseSearchDialog(uiState, viewModel::modifierMotCle, viewModel::chercherExercices,
                viewModel::ajouterExercice, viewModel::fermerRecherche)
        }
        if (choisirDate) {
            SelecteurDate(
                dateInitiale = uiState.datePrevue,
                onConfirmer = { date ->
                    viewModel.modifierDate(date)
                    choisirDate = false
                    // Enchaîne avec l'heure : un seul parcours pour « date et heure prévues ».
                    choisirHeure = true
                },
                onDismiss = { choisirDate = false }
            )
        }
        if (choisirHeure) {
            SelecteurHeure(
                dateInitiale = uiState.datePrevue,
                onConfirmer = { heure, minute ->
                    viewModel.modifierHeure(heure, minute)
                    choisirHeure = false
                },
                onDismiss = { choisirHeure = false }
            )
        }
        if (expliquerNotifications) {
            AlertDialog(
                onDismissRequest = { expliquerNotifications = false },
                title = { Text(stringResource(R.string.permission_notif_titre)) },
                text = { Text(stringResource(R.string.permission_notif_explication)) },
                confirmButton = {
                    TextButton(onClick = {
                        expliquerNotifications = false
                        demandeNotifications.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }) { Text(stringResource(R.string.btn_continuer)) }
                },
                dismissButton = {
                    TextButton(onClick = {
                        expliquerNotifications = false
                        notificationsRefusees = true
                    }) { Text(stringResource(R.string.btn_plus_tard)) }
                }
            )
        }
    }
}

/** Sélecteur de date : seules les dates à partir d'aujourd'hui sont proposées. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelecteurDate(
    dateInitiale: Long?,
    onConfirmer: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val aujourdhuiUtc = LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    val etat = rememberDatePickerState(
        // Le DatePicker travaille en minuit UTC du jour choisi.
        initialSelectedDateMillis = dateInitiale?.let {
            Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                .atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        },
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis >= aujourdhuiUtc
        }
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = { etat.selectedDateMillis?.let(onConfirmer) },
                enabled = etat.selectedDateMillis != null
            ) { Text(stringResource(R.string.btn_ok)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.btn_annuler)) } }
    ) {
        DatePicker(state = etat)
    }
}

/** Sélecteur d'heure qui respecte le réglage 12 h / 24 h de l'appareil. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelecteurHeure(
    dateInitiale: Long?,
    onConfirmer: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val contexte = LocalContext.current
    val heureInitiale = dateInitiale?.let { Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalTime() }
        ?: LocalTime.now().plusHours(1).withMinute(0)
    val etat = rememberTimePickerState(
        initialHour = heureInitiale.hour,
        initialMinute = heureInitiale.minute,
        is24Hour = DateFormat.is24HourFormat(contexte)
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.form_choisir_heure)) },
        text = {
            // TimeInput (saisie au clavier) reste lisible avec une police agrandie,
            // contrairement au cadran du TimePicker.
            if (LocalDensity.current.fontScale > 1.3f) TimeInput(state = etat) else TimePicker(state = etat)
        },
        confirmButton = {
            TextButton(onClick = { onConfirmer(etat.hour, etat.minute) }) { Text(stringResource(R.string.btn_ok)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.btn_annuler)) } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateWorkoutContent(
    uiState: CreateWorkoutUiState,
    modifier: Modifier = Modifier,
    menuOuvert: Boolean,
    onMenuChange: (Boolean) -> Unit,
    onTitreChange: (String) -> Unit,
    onTypeChange: (EnumTypeActivite) -> Unit,
    onLieuChange: (String) -> Unit,
    onIsOutsideChange: (Boolean) -> Unit,
    onIntensiteChange: (Int) -> Unit,
    onNotesChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    formulaireValide: Boolean,
    onChoisirDate: () -> Unit = {},
    onChoisirHeure: () -> Unit = {},
    onEffacerDate: () -> Unit = {},
    onRappelChange: (Boolean) -> Unit = {},
    notificationsRefusees: Boolean = false,
    onAddExercise: () -> Unit = {},
    onExerciseChange: (Int, String, String, String) -> Unit = { _, _, _, _ -> },
    onDeleteExercise: (Int) -> Unit = {}
) {
    val typeSelectionne = stringResource(uiState.typeActivite.libelleRes())

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = uiState.titre,
            onValueChange = onTitreChange,
            enabled = !uiState.isSaving,
            label = { Text(stringResource(R.string.form_titre)) },
            isError = uiState.erreurTitre,
            supportingText = {
                if (uiState.erreurTitre) {
                    Text(stringResource(R.string.titre_min_3))
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        ExposedDropdownMenuBox(
            expanded = menuOuvert,
            onExpandedChange = { if (!uiState.isSaving) onMenuChange(it) },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = typeSelectionne,
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.form_type)) },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuOuvert)
                },
                modifier = Modifier
                    .menuAnchor(
                        type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                        enabled = true
                    )
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = menuOuvert,
                onDismissRequest = { onMenuChange(false) }
            ) {
                EnumTypeActivite.entries.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(stringResource(type.libelleRes())) },
                        onClick = { onTypeChange(type) }
                    )
                }
            }
        }

        OutlinedTextField(
            value = uiState.lieu,
            onValueChange = onLieuChange,
            enabled = !uiState.isSaving,
            label = { Text(stringResource(R.string.form_lieu)) },
            isError = uiState.erreurLieu,
            supportingText = {
                if (uiState.erreurLieu) {
                    Text(stringResource(R.string.lieu_obligatoire))
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        LigneInterrupteur(
            texte = stringResource(R.string.form_exterieur),
            coche = uiState.isOutside,
            onChange = onIsOutsideChange,
            actif = !uiState.isSaving
        )

        SectionPlanification(
            uiState = uiState,
            onChoisirDate = onChoisirDate,
            onChoisirHeure = onChoisirHeure,
            onEffacerDate = onEffacerDate,
            onRappelChange = onRappelChange,
            notificationsRefusees = notificationsRefusees
        )

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.form_intensite, uiState.intensite))
            Slider(
                value = uiState.intensite.toFloat(),
                onValueChange = { valeur ->
                    onIntensiteChange(valeur.roundToInt())
                },
                valueRange = 1f..10f,
                enabled = !uiState.isSaving,
                steps = 8
            )
        }

        OutlinedTextField(
            value = uiState.notes,
            onValueChange = onNotesChange,
            enabled = !uiState.isSaving,
            label = { Text(stringResource(R.string.form_notes)) },
            minLines = 3,
            modifier = Modifier.fillMaxWidth()
        )

        if (uiState.typeActivite == EnumTypeActivite.Musculation) {
            Text(stringResource(R.string.exercices_musculation), style = MaterialTheme.typography.titleLarge)
            uiState.exercices.forEachIndexed { index, exercice ->
                ExerciseEditor(exercice,
                    onChange = { series, repetitions, charge ->
                        onExerciseChange(index, series, repetitions, charge)
                    }, onDelete = { onDeleteExercise(index) }, enabled = !uiState.isSaving)
            }
            Button(onClick = onAddExercise, enabled = !uiState.isSaving && !uiState.chargementImpossible) {
                Text(stringResource(R.string.exercice_ajouter))
            }
        }

        Button(
            onClick = onSaveClick,
            enabled = formulaireValide && !uiState.isSaving,
            modifier = Modifier.align(Alignment.End)
        ) {
            Text(
                if (uiState.isSaving) {
                    stringResource(R.string.btn_enregistrement)
                } else {
                    stringResource(R.string.btn_enregistrer)
                }
            )
        }

        if (uiState.erreur != null) {
            Text(
                text = uiState.erreur.asString(),
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SectionPlanification(
    uiState: CreateWorkoutUiState,
    onChoisirDate: () -> Unit,
    onChoisirHeure: () -> Unit,
    onEffacerDate: () -> Unit,
    onRappelChange: (Boolean) -> Unit,
    notificationsRefusees: Boolean
) {
    val locale = localeCourante()
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(R.string.form_date_prevue), style = MaterialTheme.typography.titleMedium)
        Text(
            text = uiState.datePrevue?.let { formaterDateHeure(it, locale) }
                ?: stringResource(R.string.form_date_aucune),
            color = if (uiState.erreurDate != null) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.onSurface
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(onClick = onChoisirDate, enabled = !uiState.isSaving) {
                Text(stringResource(R.string.form_choisir_date))
            }
            OutlinedButton(onClick = onChoisirHeure, enabled = !uiState.isSaving) {
                Text(stringResource(R.string.form_choisir_heure))
            }
            if (uiState.datePrevue != null && !uiState.rappelActif) {
                TextButton(onClick = onEffacerDate, enabled = !uiState.isSaving) {
                    Text(stringResource(R.string.form_effacer_date))
                }
            }
        }

        LigneInterrupteur(
            texte = stringResource(R.string.form_rappel),
            coche = uiState.rappelActif,
            onChange = onRappelChange,
            actif = !uiState.isSaving
        )
        uiState.erreurDate?.let { erreur ->
            Text(stringResource(erreur), color = MaterialTheme.colorScheme.error)
        }
        if (uiState.rappelActif && notificationsRefusees) {
            Text(stringResource(R.string.permission_notif_refusee), color = MaterialTheme.colorScheme.error)
        }
    }
}

@Preview(name = "Mode clair", uiMode = UI_MODE_NIGHT_NO, showBackground = true)
@Preview(name = "Mode sombre", uiMode = UI_MODE_NIGHT_YES, showBackground = true)
@Preview(name = "Police agrandie", fontScale = 2f, showBackground = true, heightDp = 1600)
@Composable
fun CreateWorkoutPreview() {
    ApexTheme {
        CreateWorkoutContent(
            uiState = CreateWorkoutUiState(
                titre = stringResource(R.string.preview_jambes),
                typeActivite = EnumTypeActivite.Musculation,
                lieu = stringResource(R.string.preview_gym),
                isOutside = false,
                intensite = 7,
                notes = stringResource(R.string.preview_jambes_notes),
                datePrevue = System.currentTimeMillis() + 86_400_000L,
                rappelActif = true
            ),
            menuOuvert = false,
            onMenuChange = {},
            onTitreChange = {},
            onTypeChange = {},
            onLieuChange = {},
            onIsOutsideChange = {},
            onIntensiteChange = {},
            onNotesChange = {},
            onSaveClick = {},
            formulaireValide = true
        )
    }
}
