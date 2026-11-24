package com.example.apex.ui.components

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.apex.R
import com.example.apex.ui.theme.ApexTheme

@Composable
fun BtnFavori(
    favori: Boolean,
    onFavClick: () -> Unit
) {
    IconButton(onClick = onFavClick) {
        // La description décrit l'action ; la forme (pleine / contour) distingue l'état
        // sans dépendre uniquement de la couleur.
        Icon(
            imageVector = if (favori) Icons.Filled.Star else Icons.Outlined.StarBorder,
            contentDescription = stringResource(
                if (favori) R.string.cd_favori_retirer else R.string.cd_favori_ajouter
            ),
            tint = if (favori) Color(0xFFBD8328) else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(name = "Mode clair", uiMode = UI_MODE_NIGHT_NO, showBackground = true)
@Preview(name = "Mode sombre", uiMode = UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun BtnFavoriPreview() {
    ApexTheme {
        BtnFavori(
            favori = true,
            onFavClick = {}
        )
    }
}
