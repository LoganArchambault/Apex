package com.example.apex.network

import com.example.apex.BuildConfig
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

object RetrofitInstance {
    private val json = Json { ignoreUnknownKeys = true }

    fun creerService(apiKey: String, baseUrl: String = "https://movebase.p.rapidapi.com/"):
        MoveBaseApiService {
        val client = OkHttpClient.Builder().addInterceptor { chaine ->
            chaine.proceed(chaine.request().newBuilder()
                .header("X-RapidAPI-Key", apiKey)
                .header("X-RapidAPI-Host", "movebase.p.rapidapi.com")
                .build())
        }.build()
        return Retrofit.Builder().baseUrl(baseUrl).client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build().create(MoveBaseApiService::class.java)
    }

    val api: MoveBaseApiService by lazy { creerService(BuildConfig.MOVEBASE_API_KEY) }
}
