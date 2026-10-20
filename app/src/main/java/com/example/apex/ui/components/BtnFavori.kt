package com.example.apex.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
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
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = stringResource(R.string.cd_favori),
            tint = if (favori) Color(0xFFBD8328) else Color.Gray
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BtnFavoriPreview() {
    ApexTheme {
        BtnFavori(
            favori = true,
            onFavClick = {}
        )
    }
}
