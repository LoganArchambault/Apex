package com.example.apex.ui.components

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import com.example.apex.ui.theme.ApexTheme
import com.example.apex.data.Exercise
import androidx.compose.ui.res.stringResource
import com.example.apex.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.apex.viewmodel.ExerciceFormState

@Composable
fun ExerciseEditor(
    etat: ExerciceFormState,
    onChange: (String, String, String) -> Unit,
    onDelete: () -> Unit,
    enabled: Boolean = true
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(etat.exercice.nom, style = MaterialTheme.typography.titleMedium)
            Text(listOf(etat.exercice.muscleCible, etat.exercice.equipement)
                .filter { it.isNotBlank() }.joinToString(stringResource(R.string.separateur_caracteristiques)))
            if (etat.exercice.instructions.isNotBlank()) Text(etat.exercice.instructions)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = etat.series,
                    onValueChange = { onChange(it, etat.repetitions, etat.charge) },
                    label = { Text(stringResource(R.string.exercice_series)) }, modifier = Modifier.weight(1f),
                    enabled = enabled, singleLine = true,
                    isError = etat.series.toIntOrNull()?.let { it > 0 } != true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = etat.repetitions,
                    onValueChange = { onChange(etat.series, it, etat.charge) },
                    label = { Text(stringResource(R.string.exercice_repetitions)) }, modifier = Modifier.weight(1f),
                    enabled = enabled, singleLine = true,
                    isError = etat.repetitions.toIntOrNull()?.let { it > 0 } != true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            }
            OutlinedTextField(value = etat.charge,
                onValueChange = { onChange(etat.series, etat.repetitions, it) },
                label = { Text(stringResource(R.string.exercice_charge)) }, modifier = Modifier.fillMaxWidth(),
                enabled = enabled, singleLine = true,
                isError = etat.charge.replace(',', '.').toDoubleOrNull()
                    ?.let { it.isFinite() && it >= 0 } != true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
            if (etat.versModele() == null) {
                Text(stringResource(R.string.exercice_validation),
                    color = MaterialTheme.colorScheme.error)
            }
            TextButton(onClick = onDelete, enabled = enabled) { Text(stringResource(R.string.exercice_retirer)) }
        }
    }
}

@Preview(name = "Mode clair", uiMode = UI_MODE_NIGHT_NO, showBackground = true)
@Preview(name = "Mode sombre", uiMode = UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun ExerciseEditorPreview() {
    ApexTheme {
        ExerciseEditor(
            etat = ExerciceFormState(
                exercice = Exercise(
                id = "0025",
                nom = stringResource(R.string.preview_exercice_nom),
                muscleCible = stringResource(R.string.preview_exercice_muscle),
                equipement = stringResource(R.string.preview_exercice_equipement),
                instructions = stringResource(R.string.preview_exercice_instructions)
            ),
                series = "3",
                repetitions = "10",
                charge = "20"
            ),
            onChange = { _, _, _ -> },
            onDelete = {}
        )
    }
}
