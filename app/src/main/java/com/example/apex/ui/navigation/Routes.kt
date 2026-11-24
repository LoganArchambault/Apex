package com.example.apex.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
object HomeRoute

@Serializable
object WorkoutsRoute

@Serializable
object CreateWorkoutRoute

@Serializable
data class DetailsRoute(val idWorkout: Int)

@Serializable
data class EditWorkoutRoute(val idWorkout: Int)

@Serializable
object ParametresRoute
