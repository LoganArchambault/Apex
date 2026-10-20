package com.example.apex.ui.screens

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
import androidx.compose.ui.tooling.preview.AndroidUiModes
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.apex.R
import com.example.apex.data.EnumTypeActivite
import com.example.apex.data.Workout
import com.example.apex.repository.FakeWorkoutData
import com.example.apex.ui.components.WorkoutCard
import com.example.apex.ui.theme.ApexTheme
import com.example.apex.viewmodel.WorkoutUiState
import com.example.apex.viewmodel.WorkoutViewModel

@Composable
fun WorkoutsScreen(
    viewModel: WorkoutViewModel = viewModel(),
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
                                label = { Text(nomTypeActiviteListe(type)) }
                            )
                        }
                    }

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
        items(workouts) { workout ->
            WorkoutCard(
                workout = workout,
                onFavoriToggle = { onFavoriToggle(workout) },
                onCardClick = { onDetailClick(workout) }
            )
        }
    }
}

@Composable
private fun nomTypeActiviteListe(type: EnumTypeActivite): String {
    return when (type) {
        EnumTypeActivite.Course -> stringResource(R.string.activite_course)
        EnumTypeActivite.Velo -> stringResource(R.string.activite_velo)
        EnumTypeActivite.Natation -> stringResource(R.string.activite_natation)
        EnumTypeActivite.Musculation -> stringResource(R.string.activite_musculation)
    }
}

@Preview(name = "Mode clair", showBackground = true)
@Preview(name = "Mode sombre", uiMode = AndroidUiModes.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun EcranWorkoutPreview() {
    ApexTheme {
        WorkoutContent(
            uiState = WorkoutUiState(
                workouts = FakeWorkoutData.fullList,
                filteredWorkouts = FakeWorkoutData.fullList
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
