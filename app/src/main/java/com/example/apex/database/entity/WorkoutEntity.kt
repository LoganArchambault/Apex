package com.example.apex.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.apex.data.EnumTypeActivite

@Entity(tableName = "workouts")
data class WorkoutEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val titre: String,
    val typeActivite: EnumTypeActivite,
    val lieu: String,
    val isOutside: Boolean,
    val intensite: Int,
    val notes: String,
    val done: Boolean,
    val favori: Boolean,
    // Ajouts de la version 2 (voir MIGRATION_1_2).
    // Date prévue en millisecondes UTC (Instant), convertie au fuseau local seulement à l'affichage.
    val datePrevue: Long? = null,
    @ColumnInfo(defaultValue = "0") val rappelActif: Boolean = false,
    @ColumnInfo(defaultValue = "0") val rappelEnvoye: Boolean = false,
    val distanceMetres: Double? = null,
    val dureeSecondes: Long? = null
)
