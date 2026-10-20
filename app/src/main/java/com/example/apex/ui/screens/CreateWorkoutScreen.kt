package com.example.apex.ui.screens

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
import androidx.compose.material3.Switch
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateWorkoutScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    onWorkoutCreated: () -> Unit,
    viewModel: CreateWorkoutViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var menuOuvert by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.form_titre_ecran)) },
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
            formulaireValide = viewModel.estValide()
        )
    }
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
    formulaireValide: Boolean
) {
    val typeSelectionne = nomTypeActivite(uiState.typeActivite)

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
            onExpandedChange = onMenuChange,
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
                        text = { Text(nomTypeActivite(type)) },
                        onClick = { onTypeChange(type) }
                    )
                }
            }
        }

        OutlinedTextField(
            value = uiState.lieu,
            onValueChange = onLieuChange,
            label = { Text(stringResource(R.string.form_lieu)) },
            isError = uiState.erreurLieu,
            supportingText = {
                if (uiState.erreurLieu) {
                    Text(stringResource(R.string.lieu_obligatoire))
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(R.string.form_exterieur))
            Switch(
                checked = uiState.isOutside,
                onCheckedChange = onIsOutsideChange
            )
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.form_intensite, uiState.intensite))
            Slider(
                value = uiState.intensite.toFloat(),
                onValueChange = { valeur ->
                    onIntensiteChange(valeur.roundToInt())
                },
                valueRange = 1f..10f,
                steps = 8
            )
        }

        OutlinedTextField(
            value = uiState.notes,
            onValueChange = onNotesChange,
            label = { Text(stringResource(R.string.form_notes)) },
            minLines = 3,
            modifier = Modifier.fillMaxWidth()
        )

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
                text = stringResource(R.string.erreur_generique),
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun nomTypeActivite(type: EnumTypeActivite): String {
    return when (type) {
        EnumTypeActivite.Course -> stringResource(R.string.activite_course)
        EnumTypeActivite.Velo -> stringResource(R.string.activite_velo)
        EnumTypeActivite.Natation -> stringResource(R.string.activite_natation)
        EnumTypeActivite.Musculation -> stringResource(R.string.activite_musculation)
    }
}

@Preview(name = "Mode clair", showBackground = true)
@Preview(
    name = "Mode sombre",
    uiMode = UI_MODE_NIGHT_YES,
    showBackground = true
)
@Composable
fun CreateWorkoutPreview() {
    ApexTheme {
        CreateWorkoutContent(
            uiState = CreateWorkoutUiState(
                titre = "Entraînement jambes",
                typeActivite = EnumTypeActivite.Musculation,
                lieu = "Gym",
                isOutside = false,
                intensite = 7,
                notes = "Hip thrust, leg press et abductions"
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
