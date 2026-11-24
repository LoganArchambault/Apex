package com.example.apex.ui.components

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import com.example.apex.ui.theme.ApexTheme
import androidx.compose.ui.res.stringResource
import com.example.apex.R
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.apex.data.Exercise
import com.example.apex.viewmodel.CreateWorkoutUiState

@Composable
fun ExerciseSearchDialog(
    etat: CreateWorkoutUiState,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onSelect: (Exercise) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.exercice_ajouter)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.exercice_langue_info))
                OutlinedTextField(value = etat.motCle, onValueChange = onQueryChange,
                    label = { Text(stringResource(R.string.exercice_mot_cle)) }, singleLine = true,
                    modifier = Modifier.fillMaxWidth())
                Button(onClick = onSearch,
                    enabled = etat.motCle.isNotBlank() && !etat.rechercheEnCours) {
                    Text(stringResource(R.string.recherche))
                }
                if (etat.rechercheEnCours) CircularProgressIndicator()
                etat.erreurRecherche?.let { Text(it.asString(), color = MaterialTheme.colorScheme.error) }
                if (etat.rechercheEffectuee && etat.resultats.isEmpty()) {
                    Text(stringResource(R.string.exercice_recherche_vide))
                }
                LazyColumn(Modifier.heightIn(max = 320.dp)) {
                    items(etat.resultats, key = { it.id }) { exercice ->
                        Column(Modifier.fillMaxWidth().clickable { onSelect(exercice) }.padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(exercice.nom, style = MaterialTheme.typography.titleSmall)
                            Text(listOf(exercice.muscleCible, exercice.equipement)
                                .filter { it.isNotBlank() }.joinToString(stringResource(R.string.separateur_caracteristiques)))
                            Text(exercice.instructions, maxLines = 3)
                            Text(stringResource(R.string.btn_choisir), color = MaterialTheme.colorScheme.primary)
                        }
                        HorizontalDivider()
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.btn_fermer)) } }
    )
}

@Preview(name = "Mode clair", uiMode = UI_MODE_NIGHT_NO, showBackground = true)
@Preview(name = "Mode sombre", uiMode = UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun ExerciseSearchDialogPreview() {
    ApexTheme {
        ExerciseSearchDialog(
            etat = CreateWorkoutUiState(
                rechercheOuverte = true,
                motCle = stringResource(R.string.preview_exercice_recherche),
                rechercheEffectuee = true,
                resultats = listOf(Exercise(
                id = "0025",
                nom = stringResource(R.string.preview_exercice_nom),
                muscleCible = stringResource(R.string.preview_exercice_muscle),
                equipement = stringResource(R.string.preview_exercice_equipement),
                instructions = stringResource(R.string.preview_exercice_instructions)
            ))
            ),
            onQueryChange = {},
            onSearch = {},
            onSelect = {},
            onDismiss = {}
        )
    }
}
