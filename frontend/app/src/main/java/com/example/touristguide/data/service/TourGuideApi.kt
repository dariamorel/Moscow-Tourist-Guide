package com.example.touristguide.data.service

import com.example.touristguide.data.dto.AchievementDto
import com.example.touristguide.data.dto.LoginResponseDto
import com.example.touristguide.data.dto.OptimizeRouteInputDto
import com.example.touristguide.data.dto.OptimizeRouteResponseDto
import com.example.touristguide.data.dto.RouteDto
import com.example.touristguide.data.dto.RouteInputDto
import com.example.touristguide.data.dto.RouteUpdateInputDto
import com.example.touristguide.data.dto.SightDto
import com.example.touristguide.data.dto.UserDto
import com.example.touristguide.data.dto.UserInputDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface TourGuideApi {

    @GET("sights")
    suspend fun getSights(
        @Query("limit") limit: Int? = null,
        @Query("offset") offset: Int? = null,
        @Query("tag") tag: String? = null
    ): List<SightDto>

    @GET("routes")
    suspend fun getRoutes(
        @Header("Authorization") authorization: String,
        @Query("user_id") userId: Int,
        @Query("status") status: String? = null
    ): List<RouteDto>

    @POST("routes")
    suspend fun createRoute(
        @Header("Authorization") authorization: String,
        @Body body: RouteInputDto
    ): RouteDto

    @PUT("routes/{routeId}")
    suspend fun updateRoute(
        @Header("Authorization") authorization: String,
        @Path("routeId") routeId: Int,
        @Body body: RouteUpdateInputDto
    ): RouteDto

    @POST("routes/optimize")
    suspend fun optimizeRoute(
        @Body body: OptimizeRouteInputDto
    ): OptimizeRouteResponseDto

    @GET("achievements")
    suspend fun getAchievements(
        @Query("user_id") userId: Int? = null
    ): List<AchievementDto>

    @POST("users/registration")
    suspend fun register(
        @Body body: UserInputDto
    ): UserDto

    @POST("users/login")
    suspend fun login(
        @Body body: UserInputDto
    ): LoginResponseDto
}
