package com.example.apex.util

import android.text.format.DateUtils
import androidx.annotation.StringRes
import com.example.apex.R
import com.example.apex.data.EnumTypeActivite
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/*
 * Formatage régional (i18n) : aucun format n'est figé dans le code. Ces fonctions pures sont
 * utilisées autant par les composables (locale de LocalConfiguration) que par le Worker et le
 * service (Locale.getDefault()), qui n'ont pas accès à Compose.
 */

/** Date et heure prévues : instant UTC converti au fuseau de l'appareil, style de la locale. */
fun formaterDateHeure(millisUtc: Long, locale: Locale): String =
    Instant.ofEpochMilli(millisUtc)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT).withLocale(locale))

fun formaterDate(millisUtc: Long, locale: Locale): String =
    Instant.ofEpochMilli(millisUtc)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale))

fun formaterHeure(millisUtc: Long, locale: Locale): String =
    Instant.ofEpochMilli(millisUtc)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(locale))

/** Durée « MM:SS » ou « H:MM:SS ». */
fun formaterDuree(secondes: Long): String = DateUtils.formatElapsedTime(secondes)

/** Distance prête à afficher : nombre formaté selon la locale + ressource de l'unité. */
data class DistanceAffichee(val valeur: String, @param:StringRes val unite: Int)

fun distanceLocalisee(metres: Double, locale: Locale): DistanceAffichee {
    // Toujours en kilomètres ; seul le séparateur décimal suit la locale (« 3,50 » / « 3.50 »).
    val texte = NumberFormat.getNumberInstance(locale).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }.format(metres / 1000.0)
    return DistanceAffichee(texte, R.string.distance_km)
}

/** Nombre décimal (ex. : une charge) selon la locale : « 32,5 » (fr) · « 32.5 » (en). */
fun formaterNombre(valeur: Double, locale: Locale): String =
    NumberFormat.getNumberInstance(locale).apply { maximumFractionDigits = 2 }.format(valeur)

@StringRes
fun EnumTypeActivite.libelleRes(): Int = when (this) {
    EnumTypeActivite.Course -> R.string.activite_course
    EnumTypeActivite.Velo -> R.string.activite_velo
    EnumTypeActivite.Natation -> R.string.activite_natation
    EnumTypeActivite.Musculation -> R.string.activite_musculation
}

/** Langue demandée à l'API MoveBase : suit la langue de l'application (français ou anglais). */
fun langueApplication(locale: Locale = Locale.getDefault()): String =
    if (locale.language == "fr") "fr" else "en"
