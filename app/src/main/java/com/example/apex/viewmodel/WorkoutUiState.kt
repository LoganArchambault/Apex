package com.example.apex.viewmodel

import com.example.apex.data.EnumTypeActivite
import com.example.apex.data.Workout

data class WorkoutUiState(
    val workouts: List<Workout> = emptyList(),
    val filteredWorkouts: List<Workout> = emptyList(),
    val recherche: String = "",
    val favorisSeulement: Boolean = false,
    val typeSelectionne: EnumTypeActivite? = null,
    val completedCount: Int = 0,
    val totalCount: Int = 0,
    val completionProgress: Float = 0f,
    val nextWorkout: Workout? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
