package com.example.apex.ui.screens

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.apex.R
import com.example.apex.security.DisponibiliteAuth
import com.example.apex.ui.components.LigneInterrupteur
import com.example.apex.ui.theme.ApexTheme
import com.example.apex.viewmodel.ParametresViewModel
import com.example.apex.viewmodel.WorkoutViewModelFactory

@Composable
fun ParametresScreen(
    lireDisponibilite: () -> DisponibiliteAuth,
    authentifier: (onSucces: () -> Unit) -> Unit,
    viewModel: ParametresViewModel = viewModel(factory = WorkoutViewModelFactory.parametres)
) {
    val verrouillageActif by viewModel.verrouillageActif.collectAsStateWithLifecycle()
    // Relue au retour dans l'écran : l'utilisateur a pu configurer un NIP entre-temps.
    var disponibilite by remember { mutableStateOf(lireDisponibilite()) }
    LifecycleResumeEffect(Unit) {
        disponibilite = lireDisponibilite()
        onPauseOrDispose { }
    }

    ParametresContent(
        verrouillageActif = verrouillageActif,
        disponibilite = disponibilite,
        onVerrouillageChange = { actif ->
            // Activer exige une authentification réussie ; désactiver est immédiat.
            if (actif) authentifier { viewModel.modifierVerrouillage(true) }
            else viewModel.modifierVerrouillage(false)
        }
    )
}

@Composable
private fun ParametresContent(
    verrouillageActif: Boolean,
    disponibilite: DisponibiliteAuth,
    onVerrouillageChange: (Boolean) -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(stringResource(R.string.parametres_titre), style = MaterialTheme.typography.headlineSmall)

            val disponible = disponibilite == DisponibiliteAuth.DISPONIBLE
            LigneInterrupteur(
                texte = stringResource(R.string.parametres_verrouiller),
                coche = verrouillageActif && disponible,
                actif = disponible,
                onChange = onVerrouillageChange
            )
            Text(
                text = stringResource(
                    when (disponibilite) {
                        DisponibiliteAuth.DISPONIBLE -> R.string.parametres_verrouiller_description
                        DisponibiliteAuth.AUCUN_IDENTIFIANT -> R.string.parametres_verrouiller_aucun_identifiant
                        DisponibiliteAuth.NON_SUPPORTEE -> R.string.parametres_verrouiller_non_supporte
                    }
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = if (disponible) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.error
            )
        }
    }
}

@Preview(name = "Mode clair", uiMode = UI_MODE_NIGHT_NO, showBackground = true)
@Preview(name = "Mode sombre", uiMode = UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun ParametresPreview() {
    ApexTheme {
        ParametresContent(verrouillageActif = true, disponibilite = DisponibiliteAuth.DISPONIBLE,
            onVerrouillageChange = {})
    }
}

@Preview(name = "Authentification indisponible", fontScale = 2f, showBackground = true)
@Composable
fun ParametresIndisponiblePreview() {
    ApexTheme {
        ParametresContent(verrouillageActif = false, disponibilite = DisponibiliteAuth.AUCUN_IDENTIFIANT,
            onVerrouillageChange = {})
    }
}
