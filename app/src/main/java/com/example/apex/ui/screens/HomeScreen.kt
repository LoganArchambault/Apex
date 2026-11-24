package com.example.apex.ui.screens

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import com.example.apex.data.Workout
import com.example.apex.data.EnumTypeActivite
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import android.hardware.SensorManager
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.example.apex.sensors.secousses
import com.example.apex.util.libelleRes
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.apex.R
import com.example.apex.ui.components.BtnCreation
import com.example.apex.ui.components.WorkoutCard
import com.example.apex.ui.theme.ApexTheme
import com.example.apex.viewmodel.WorkoutUiState
import com.example.apex.viewmodel.WorkoutViewModelFactory
import com.example.apex.viewmodel.WorkoutViewModel

@Composable
fun HomeScreen(
    viewModel: WorkoutViewModel = viewModel(factory = WorkoutViewModelFactory.liste),
    onAllWorkoutsClick: () -> Unit = {},
    onDetailClick: (Int) -> Unit = {},
    onCreateClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val contexte = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Écoute de l'accéléromètre seulement quand le tableau de bord est visible (STARTED) :
    // repeatOnLifecycle annule la collecte, ce qui désinscrit l'écouteur automatiquement.
    // Sans accéléromètre, secousses() se termine sans rien émettre : aucune erreur.
    LaunchedEffect(lifecycleOwner) {
        val capteurs = contexte.getSystemService(SensorManager::class.java) ?: return@LaunchedEffect
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            capteurs.secousses().collect {
                if (!viewModel.uiState.value.afficherSuggestion) viewModel.suggererSeance()
            }
        }
    }

    HomeContent(
        uiState = uiState,
        onAllWorkoutsClick = onAllWorkoutsClick,
        onCreateClick = onCreateClick,
        onDetailClick = onDetailClick,
        onFavoriToggle = viewModel::toggleFavori,
        onSurprenezMoiClick = viewModel::suggererSeance
    )

    if (uiState.afficherSuggestion) {
        SuggestionDialog(
            suggestion = uiState.suggestion,
            onVoirFiche = { id ->
                viewModel.fermerSuggestion()
                onDetailClick(id)
            },
            onDismiss = viewModel::fermerSuggestion
        )
    }
}

@Composable
private fun SuggestionDialog(
    suggestion: Workout?,
    onVoirFiche: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.suggestion_titre)) },
        text = {
            Text(
                if (suggestion == null) stringResource(R.string.suggestion_aucune)
                else stringResource(R.string.suggestion_texte, suggestion.titre,
                    stringResource(suggestion.typeActivite.libelleRes()), suggestion.lieu)
            )
        },
        confirmButton = {
            if (suggestion != null) {
                TextButton(onClick = { onVoirFiche(suggestion.id) }) {
                    Text(stringResource(R.string.suggestion_voir_fiche))
                }
            } else {
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.btn_fermer)) }
            }
        },
        dismissButton = {
            if (suggestion != null) {
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.btn_fermer)) }
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HomeContent(
    uiState: WorkoutUiState,
    modifier: Modifier = Modifier,
    onAllWorkoutsClick: () -> Unit,
    onCreateClick: () -> Unit,
    onDetailClick: (Int) -> Unit,
    onFavoriToggle: (Workout) -> Unit,
    onSurprenezMoiClick: () -> Unit
) {
    Surface(modifier = modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.errorMessage != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.erreur_generique),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Text(
                                text = pluralStringResource(
                                    R.plurals.accueil_compteur,
                                    uiState.completedCount,
                                    uiState.completedCount,
                                    uiState.totalCount
                                ),
                                style = MaterialTheme.typography.bodyLarge
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = stringResource(R.string.accueil_progression),
                                style = MaterialTheme.typography.labelMedium
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            LinearProgressIndicator(
                                progress = uiState.completionProgress,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // FlowRow : avec une police agrandie, les boutons passent à la ligne
                            // au lieu d'être tronqués.
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(onClick = onAllWorkoutsClick) {
                                    Text(stringResource(R.string.btn_tous_entrainements))
                                }

                                BtnCreation(onClick = onCreateClick)

                                OutlinedButton(onClick = onSurprenezMoiClick) {
                                    Text(stringResource(R.string.btn_surprenez_moi))
                                }
                            }
                        }
                    }

                    Text(
                        text = stringResource(R.string.accueil_prochaine),
                        style = MaterialTheme.typography.titleMedium
                    )

                    val workout = uiState.nextWorkout

                    if (workout != null) {
                        WorkoutCard(
                            workout = workout,
                            onFavoriToggle = { onFavoriToggle(workout) },
                            onCardClick = onDetailClick
                        )
                    } else {
                        Card(
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = stringResource(R.string.accueil_aucune),
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(name = "Mode clair", uiMode = UI_MODE_NIGHT_NO, showBackground = true)
@Preview(name = "Mode sombre", uiMode = UI_MODE_NIGHT_YES, showBackground = true)
@Preview(name = "Police agrandie", fontScale = 2f, showBackground = true)
@Composable
fun HomeScreenPreview() {
    val workouts = listOf(
        Workout(
            id = 1,
            titre = stringResource(R.string.preview_course),
            typeActivite = EnumTypeActivite.Course,
            lieu = stringResource(R.string.preview_parc),
            isOutside = true,
            intensite = 7,
            notes = stringResource(R.string.preview_bonne_seance),
            done = false,
            favori = false
        ),
        Workout(
            id = 2,
            titre = stringResource(R.string.preview_natation),
            typeActivite = EnumTypeActivite.Natation,
            lieu = stringResource(R.string.preview_piscine),
            isOutside = false,
            intensite = 8,
            notes = stringResource(R.string.preview_excellente_seance),
            done = true,
            favori = true
        ),
        Workout(
            id = 3,
            titre = stringResource(R.string.preview_velo),
            typeActivite = EnumTypeActivite.Velo,
            lieu = stringResource(R.string.preview_montagne),
            isOutside = true,
            intensite = 6,
            notes = stringResource(R.string.preview_seance_difficile),
            done = false,
            favori = true
        ),
        Workout(
            id = 4,
            titre = stringResource(R.string.preview_musculation),
            typeActivite = EnumTypeActivite.Musculation,
            lieu = stringResource(R.string.preview_salle),
            isOutside = false,
            intensite = 9,
            notes = stringResource(R.string.preview_seance_intense),
            done = true,
            favori = false
        )
    )
    val completed = workouts.count { it.done }

    ApexTheme {
        HomeContent(
            uiState = WorkoutUiState(
                workouts = workouts,
                filteredWorkouts = workouts,
                completedCount = completed,
                totalCount = workouts.size,
                completionProgress = completed.toFloat() / workouts.size,
                nextWorkout = workouts.firstOrNull { !it.done }
            ),
            onAllWorkoutsClick = {},
            onCreateClick = {},
            onDetailClick = {},
            onFavoriToggle = {},
            onSurprenezMoiClick = {}
        )
    }
}
