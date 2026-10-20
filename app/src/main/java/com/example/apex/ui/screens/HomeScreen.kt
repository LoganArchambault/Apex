package com.example.apex.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.tooling.preview.AndroidUiModes
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.apex.R
import com.example.apex.repository.FakeWorkoutData
import com.example.apex.ui.components.BtnCreation
import com.example.apex.ui.components.WorkoutCard
import com.example.apex.ui.theme.ApexTheme
import com.example.apex.viewmodel.WorkoutUiState
import com.example.apex.viewmodel.WorkoutViewModel

@Composable
fun HomeScreen(
    viewModel: WorkoutViewModel = viewModel(),
    onAllWorkoutsClick: () -> Unit = {},
    onDetailClick: (Int) -> Unit = {},
    onCreateClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeContent(
        uiState = uiState,
        onAllWorkoutsClick = onAllWorkoutsClick,
        onCreateClick = onCreateClick,
        onDetailClick = onDetailClick,
        onFavoriToggle = viewModel::toggleFavori
    )
}

@Composable
private fun HomeContent(
    uiState: WorkoutUiState,
    modifier: Modifier = Modifier,
    onAllWorkoutsClick: () -> Unit,
    onCreateClick: () -> Unit,
    onDetailClick: (Int) -> Unit,
    onFavoriToggle: (com.example.apex.data.Workout) -> Unit
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
                    modifier = Modifier.padding(16.dp),
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
                                text = stringResource(
                                    R.string.accueil_compteur,
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

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(onClick = onAllWorkoutsClick) {
                                    Text(stringResource(R.string.btn_tous_entrainements))
                                }

                                BtnCreation(onClick = onCreateClick)
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

@Preview(name = "Mode clair", showBackground = true)
@Preview(name = "Mode sombre", uiMode = AndroidUiModes.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun HomeScreenPreview() {
    val workouts = FakeWorkoutData.fullList.toList()
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
            onFavoriToggle = {}
        )
    }
}
