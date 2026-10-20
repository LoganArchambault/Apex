package com.example.apex.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.apex.R
import com.example.apex.data.EnumTypeActivite
import com.example.apex.data.Workout
import com.example.apex.viewmodel.WorkoutDetailsUiState
import com.example.apex.viewmodel.WorkoutDetailsViewModel
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import com.example.apex.ui.theme.ApexTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutDetailsScreen(
    modifier: Modifier = Modifier,
    idWorkout: Int,
    onWorkoutChanged: () -> Unit = {},
    onBackClick: () -> Unit,
    viewModel: WorkoutDetailsViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                WorkoutDetailsViewModel(idWorkout = idWorkout ?: 0)
            }
        }
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var afficherConfirmation by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            WorkoutDetailsTopAppBar(
                onBackClick = onBackClick,
                onDeleteClick = { afficherConfirmation = true }
            )
        }
    ) { innerPadding ->
        WorkoutDetailsContent(
            uiState = uiState,
            modifier = modifier.padding(innerPadding),
            onToggleComplete = {
                viewModel.toggleComplete(onWorkoutChanged)
            }
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
                onDismiss = {
                    afficherConfirmation = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkoutDetailsTopAppBar(
    onBackClick: () -> Unit,
    onDeleteClick: () -> Unit
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
    onToggleComplete: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.Start
    ) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator()
            }

            uiState.erreur != null -> {
                Text(
                    text = stringResource(R.string.erreur_generique),
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
                WorkoutDetailsCard(
                    workout = uiState.workout,
                    onToggleComplete = onToggleComplete
                )
            }
        }
    }
}

@Composable
private fun WorkoutDetailsCard(
    workout: Workout,
    onToggleComplete: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)

    ) {
        Text(
            text = workout.titre,
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            stringResource(
                R.string.detail_type,
                nomTypeActiviteDetails(workout.typeActivite)
            )
        )

        Text(
            stringResource(
                R.string.detail_lieu,
                workout.lieu,
                if (workout.isOutside) {
                    stringResource(R.string.txt_ext)
                } else {
                    stringResource(R.string.txt_int)
                }
            )
        )

        Text(
            stringResource(
                R.string.detail_intensite,
                workout.intensite
            )
        )

        Text(stringResource(R.string.detail_notes))

        Text(
            if (workout.notes.isBlank()) {
                stringResource(R.string.detail_aucune_note)
            } else {
                workout.notes
            }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(R.string.detail_completee))

            Switch(
                checked = workout.done,
                onCheckedChange = { onToggleComplete() }
            )
        }
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
private fun nomTypeActiviteDetails(type: EnumTypeActivite): String {
    return when (type) {
        EnumTypeActivite.Course -> stringResource(R.string.activite_course)
        EnumTypeActivite.Velo -> stringResource(R.string.activite_velo)
        EnumTypeActivite.Natation -> stringResource(R.string.activite_natation)
        EnumTypeActivite.Musculation -> stringResource(R.string.activite_musculation)
    }
}

@Preview(name = "Mode clair", showBackground = true)
@Composable
fun WorkoutDetailsPreview() {
    ApexTheme {
        WorkoutDetailsContent(
            uiState = WorkoutDetailsUiState(
                workout = Workout(
                    id = 1,
                    titre = "Course du matin",
                    typeActivite = EnumTypeActivite.Course,
                    lieu = "Parc",
                    isOutside = true,
                    intensite = 7,
                    notes = "Course tranquille de 5 km",
                    done = false,
                    favori = true
                ),
                isLoading = false,
                erreur = null
            ),
            onToggleComplete = {}
        )
    }
}
