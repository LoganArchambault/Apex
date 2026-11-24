package com.example.apex.network

import retrofit2.http.GET
import retrofit2.http.Query

interface MoveBaseApiService {
    @GET("api/v1/exercises")
    suspend fun chercherExercices(
        @Query("search") motCle: String,
        @Query("lang") langue: String,
        @Query("limit") limite: Int = 100
    ): MoveBaseResponse
}
