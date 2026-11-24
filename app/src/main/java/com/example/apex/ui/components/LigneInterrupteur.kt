package com.example.apex.ui.components

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.apex.R
import com.example.apex.ui.theme.ApexTheme
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.heightIn

/**
 * Libellé + Switch formant une seule cible tactile : toute la ligne est cliquable et TalkBack
 * l'annonce comme un seul interrupteur (« Me rappeler, interrupteur, activé »).
 * Le texte occupe l'espace restant et passe à la ligne avec une police agrandie.
 */
@Composable
fun LigneInterrupteur(
    texte: String,
    coche: Boolean,
    onChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    actif: Boolean = true
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .toggleable(value = coche, enabled = actif, role = Role.Switch, onValueChange = onChange),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(texte, modifier = Modifier.weight(1f))
        Switch(checked = coche, onCheckedChange = null, enabled = actif)
    }
}

@Preview(name = "Mode clair", uiMode = UI_MODE_NIGHT_NO, showBackground = true)
@Preview(name = "Mode sombre", uiMode = UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun LigneInterrupteurPreview() {
    ApexTheme {
        LigneInterrupteur(texte = stringResource(R.string.form_rappel), coche = true, onChange = {})
    }
}
