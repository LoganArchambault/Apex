package com.example.apex.ui.components

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
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

@Preview(name = "Mode clair", uiMode = UI_MODE_NIGHT_NO, showBackground = true)
@Preview(name = "Mode sombre", uiMode = UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun BtnCreationPreview() {
    ApexTheme {
        BtnCreation(
            onClick = {}
        )
    }
}