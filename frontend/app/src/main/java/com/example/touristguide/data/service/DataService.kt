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
import kotlinx.coroutines.CancellationException
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class DataService(
    private val api: TourGuideApi
) {

    suspend fun getAllSights(
        limit: Int? = null,
        offset: Int? = null,
        tag: String? = null
    ): List<SightDto> = withRetry {
        api.getSights(limit = limit, offset = offset, tag = tag)
    }

    suspend fun getRoutes(
        token: String,
        userId: Int
    ): List<RouteDto> = withRetry {
        api.getRoutes(
            authorization = bearer(token),
            userId = userId
        )
    }

    suspend fun createRoute(
        token: String,
        body: RouteInputDto
    ): RouteDto = withRetry {
        api.createRoute(
            authorization = bearer(token),
            body = body
        )
    }

    suspend fun updateRoute(
        token: String,
        routeId: Int,
        body: RouteUpdateInputDto
    ): RouteDto = withRetry {
        api.updateRoute(
            authorization = bearer(token),
            routeId = routeId,
            body = body
        )
    }

    suspend fun optimizeRoute(sightIds: List<Int>): OptimizeRouteResponseDto = withRetry {
        api.optimizeRoute(
            body = OptimizeRouteInputDto(sightIds = sightIds)
        )
    }

    suspend fun getAllAchievements(userId: Int? = null): List<AchievementDto> = withRetry {
        api.getAchievements(userId = userId)
    }

    suspend fun register(login: String, password: String): UserDto = withRetry {
        api.register(UserInputDto(login = login, password = password))
    }

    suspend fun login(login: String, password: String): LoginResponseDto = withRetry {
        api.login(UserInputDto(login = login, password = password))
    }

    private suspend fun <T> withRetry(block: suspend () -> T): T {
        var lastError: Throwable? = null

        for (attempt in 0 until MAX_ATTEMPTS) {
            try {
                return block()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                lastError = e
            }
        }

        throw lastError ?: IllegalStateException("Request failed")
    }

    private fun bearer(token: String): String = "Bearer $token"

    companion object {
        private const val BASE_URL = "http://72.56.0.83:8000/"
        private const val MAX_ATTEMPTS = 3

        fun create(): DataService {
            val client = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            return DataService(retrofit.create(TourGuideApi::class.java))
        }
    }
}
