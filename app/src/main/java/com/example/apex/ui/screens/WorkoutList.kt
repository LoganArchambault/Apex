package com.example.apex.ui.screens

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import com.example.apex.util.libelleRes
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.apex.R
import com.example.apex.data.EnumTypeActivite
import com.example.apex.data.Workout
import com.example.apex.ui.components.WorkoutCard
import com.example.apex.ui.theme.ApexTheme
import com.example.apex.viewmodel.WorkoutUiState
import com.example.apex.viewmodel.WorkoutViewModelFactory
import com.example.apex.viewmodel.WorkoutViewModel

@Composable
fun WorkoutsScreen(
    viewModel: WorkoutViewModel = viewModel(factory = WorkoutViewModelFactory.liste),
    onDetailClick: (Int) -> Unit = {},
    onCreateClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    WorkoutContent(
        uiState = uiState,
        onSearchChange = viewModel::modifierRecherche,
        onFavoritesToggle = viewModel::modifierFiltreFavoris,
        onTypeSelected = viewModel::modifierType,
        onDetailClick = { workout -> onDetailClick(workout.id) },
        onFavoriToggle = viewModel::toggleFavori,
        onCreateClick = onCreateClick
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WorkoutContent(
    uiState: WorkoutUiState,
    onSearchChange: (String) -> Unit,
    onFavoritesToggle: (Boolean) -> Unit,
    onTypeSelected: (EnumTypeActivite?) -> Unit,
    onDetailClick: (Workout) -> Unit,
    onFavoriToggle: (Workout) -> Unit,
    onCreateClick: () -> Unit
) {
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
                modifier = Modifier.fillMaxSize(),
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
            Scaffold(
                floatingActionButton = {
                    FloatingActionButton(onClick = onCreateClick) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = stringResource(R.string.cd_creer)
                        )
                    }
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.recherche,
                        onValueChange = onSearchChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.recherche)) },
                        singleLine = true
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = uiState.favorisSeulement,
                            onClick = {
                                onFavoritesToggle(!uiState.favorisSeulement)
                            },
                            label = { Text(stringResource(R.string.filtre_favoris)) }
                        )

                        FilterChip(
                            selected = uiState.typeSelectionne == null,
                            onClick = { onTypeSelected(null) },
                            label = { Text(stringResource(R.string.filtre_tous)) }
                        )

                        EnumTypeActivite.entries.forEach { type ->
                            FilterChip(
                                selected = uiState.typeSelectionne == type,
                                onClick = { onTypeSelected(type) },
                                label = { Text(stringResource(type.libelleRes())) }
                            )
                        }
                    }

                    Text(
                        text = pluralStringResource(R.plurals.nb_seances_affichees,
                            uiState.filteredWorkouts.size, uiState.filteredWorkouts.size),
                        style = MaterialTheme.typography.labelLarge
                    )

                    if (uiState.filteredWorkouts.isEmpty()) {
                        Text(stringResource(R.string.aucun_resultat))
                    } else {
                        WorkoutsList(
                            workouts = uiState.filteredWorkouts,
                            onDetailClick = onDetailClick,
                            onFavoriToggle = onFavoriToggle
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkoutsList(
    workouts: List<Workout>,
    modifier: Modifier = Modifier,
    onDetailClick: (Workout) -> Unit,
    onFavoriToggle: (Workout) -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(workouts, key = { it.id }) { workout ->
            WorkoutCard(
                workout = workout,
                onFavoriToggle = { onFavoriToggle(workout) },
                onCardClick = { onDetailClick(workout) }
            )
        }
    }
}


@Preview(name = "Mode clair", uiMode = UI_MODE_NIGHT_NO, showBackground = true)
@Preview(name = "Mode sombre", uiMode = UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun EcranWorkoutPreview() {
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
    ApexTheme {
        WorkoutContent(
            uiState = WorkoutUiState(
                workouts = workouts,
                filteredWorkouts = workouts
            ),
            onSearchChange = {},
            onFavoritesToggle = {},
            onTypeSelected = {},
            onDetailClick = {},
            onFavoriToggle = {},
            onCreateClick = {}
        )
    }
}
