package com.example.smartfit.network

import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

data class FoodSearchResponse(
    @SerializedName("results")
    val results: List<FoodSearchItem>
)

data class FoodSearchItem(
    val id: Int,
    val name: String,
    val image: String?
)

data class FoodDetailResponse(
    val id: Int,
    val name: String,
    val unit: String? = null,
    val nutrition: NutritionContainer
)

data class NutritionContainer(
    val nutrients: List<Nutrient>
)

data class Nutrient(
    val name: String,
    val amount: Double,
    val unit: String
)

interface SpoonacularApiService {

    @GET("food/ingredients/search")
    suspend fun searchIngredients(
        @Query("query") query: String,
        @Query("apiKey") apiKey: String,
        @Query("number") number: Int = 20
    ): FoodSearchResponse

    @GET("food/ingredients/{id}/information")
    suspend fun getIngredientInfo(
        @Path("id") id: Int,
        @Query("apiKey") apiKey: String,
        @Query("amount") amount: Int = 1
    ): FoodDetailResponse
}

object SpoonacularInstance {
    private const val BASE_URL = "https://api.spoonacular.com/"

    val api: SpoonacularApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SpoonacularApiService::class.java)
    }
}