package com.example.apex.viewmodel

import com.example.apex.data.EnumTypeActivite

data class CreateWorkoutUiState(
    val titre: String = "",
    val typeActivite: EnumTypeActivite = EnumTypeActivite.Course,
    val lieu: String = "",
    val isOutside: Boolean = false,
    val intensite: Int = 5,
    val notes: String = "",
    val erreurTitre: Boolean = false,
    val erreurLieu: Boolean = false,
    val isSaving: Boolean = false,
    val erreur: String? = null
)
