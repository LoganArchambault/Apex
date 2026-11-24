package com.example.apex.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import com.example.apex.util.distanceLocalisee
import java.util.Locale

/**
 * Locale courante de l'interface. Lue dans LocalConfiguration (observable) : un changement
 * de langue entraîne une recomposition et un nouveau formatage.
 */
@Composable
fun localeCourante(): Locale = LocalConfiguration.current.locales[0]

@Composable
fun texteDistance(metres: Double): String {
    val distance = distanceLocalisee(metres, localeCourante())
    return stringResource(distance.unite, distance.valeur)
}
