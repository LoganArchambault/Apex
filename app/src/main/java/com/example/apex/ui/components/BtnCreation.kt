package com.example.apex.ui.components

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.apex.R
import com.example.apex.ui.theme.ApexTheme

@Composable
fun BtnCreation(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier
    ) {
        Text(stringResource(R.string.btn_creation_txt))
    }
}

@Preview(name = "Bouton création")
@Composable
fun BtnCreationPreview() {
    ApexTheme{
        BtnCreation(
            onClick = {println("Bouton Création cliqué")}
        );
    }
}