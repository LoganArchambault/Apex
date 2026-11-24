package com.example.apex.repository

import com.example.apex.data.Exercise

interface IExerciseRepository {
    suspend fun chercherExercices(motCle: String): List<Exercise>
}
