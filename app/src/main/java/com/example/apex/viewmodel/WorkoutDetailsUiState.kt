package com.example.apex.viewmodel

import com.example.apex.data.Workout

data class WorkoutDetailsUiState(
    val workout: Workout? = null,
    val isLoading: Boolean = false,
    val erreur: String? = null
)
