package com.example.smartfit.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import okhttp3.OkHttpClient

data class Exercise(
    val name: String,
    val type: String,
    val muscle: String,
    val equipment: String,
    val difficulty: String,
    val instructions: String
)

interface NinjasApiService {
    @GET("exercises")
    suspend fun getExercises(
        @Query("muscle") muscle: String? = null,
        @Query("type") type: String? = null
    ): List<Exercise>
}

object NinjasRetrofitInstance {
    private const val BASE_URL = "https://api.api-ninjas.com/v1/"
    private const val API_KEY = "wqhLIC/6baSwUbr6btJ90Q==bCdCC5JqvxlK9uxV"

    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("X-Api-Key", API_KEY)
                .header("User-Agent", "SmartFitApp")
                .build()
            chain.proceed(request)
        }
        .build()

    val api: NinjasApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NinjasApiService::class.java)
    }
}