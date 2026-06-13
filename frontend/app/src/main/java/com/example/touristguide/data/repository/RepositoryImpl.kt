package com.example.touristguide.data.repository

import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.edit
import com.example.touristguide.data.dto.RouteInputDto
import com.example.touristguide.data.mapper.toDomain
import com.example.touristguide.data.mapper.toUpdateDto
import com.example.touristguide.data.service.DataService
import com.example.touristguide.domain.entities.Achievement
import com.example.touristguide.domain.entities.OptimizedSightsOrder
import com.example.touristguide.domain.entities.Route
import com.example.touristguide.domain.entities.Sight
import com.example.touristguide.domain.interfaces.Repository
import java.util.UUID

class RepositoryImpl(
    private val prefs: SharedPreferences,
    private val dataService: DataService
) : Repository {

    override suspend fun getAllSights(
        limit: Int?,
        offset: Int?,
        tag: String?
    ): List<Sight> {
        val sights = dataService.getAllSights(
            limit = limit ?: 200,
            offset = offset,
            tag = tag
        ).map { it.toDomain() }
        Log.d(Repository.REPOSITORY_TAG, "Got sights: ${sights.size}.")
        return sights
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun getAllRoutes(): List<Route> {
        val routes = dataService.getRoutes(
            token = getToken(),
            userId = requireUserId()
        ).map { it.toDomain() }
        Log.d(Repository.REPOSITORY_TAG, "Got routes: ${routes.size}.")
        return routes
    }

    override suspend fun addNewRoute(planned: List<Sight>, name: String) {
        dataService.createRoute(
            token = getToken(),
            body = RouteInputDto(
                userId = requireUserId(),
                name = name,
                plannedOrder = planned.map { it.id }
            )
        )
    }

    override suspend fun updateRoute(routeId: Int, newRoute: Route) {
        dataService.updateRoute(
            token = getToken(),
            routeId = routeId,
            body = newRoute.toUpdateDto()
        )
    }

    override suspend fun optimizeRoute(sights: List<Int>): OptimizedSightsOrder {
        val response = dataService.optimizeRoute(sights)
        Log.d(Repository.REPOSITORY_TAG, "Got optimized sights list: ${response.optimizedOrder.joinToString(" ") { it.title }}.")
        return OptimizedSightsOrder(
            sights = response.optimizedOrder.map { it.toDomain() },
            totalTime = response.totalTime
        )
    }

    override suspend fun getAllAchievements(): List<Achievement> {
        val achievements = dataService.getAllAchievements(userId = getUserId())
            .map { it.toDomain() }
        Log.d(Repository.REPOSITORY_TAG, "Got achievements: ${achievements.size}.")
        return achievements
    }

    override suspend fun register(login: String, password: String) {
        val user = dataService.register(login, password)
        saveSession(userId = user.id, login = user.login)
        Log.d(Repository.REPOSITORY_TAG, "Got user_id: ${user.id}.")
    }

    override suspend fun login(login: String, password: String) {
        val response = dataService.login(login, password)
        saveSession(userId = response.user.id, login = response.user.login)
        Log.d(Repository.REPOSITORY_TAG, "Got user_id: ${response.user.id}.")
    }

    override fun isLogin(): Boolean = prefs.contains(KEY_USER_ID)

    override fun getUserId(): Int? {
        val userId = prefs.getInt(KEY_USER_ID, -1)
        return userId.takeIf { it != -1 }
    }

    override fun logout() {
        prefs.edit {
            remove(KEY_USER_ID)
            remove(KEY_LOGIN)
        }
    }

    override fun getLogin(): String? = prefs.getString(KEY_LOGIN, null)

    private fun getToken(): String = "Test token"

    private fun requireUserId(): Int {
        return getUserId() ?: throw IllegalStateException("User is not authorized")
    }

    private fun saveSession(userId: Int, login: String) {
        prefs.edit {
            putInt(KEY_USER_ID, userId)
            putString(KEY_LOGIN, login)
        }
    }

    companion object {
        private const val KEY_USER_ID = "id"
        private const val KEY_LOGIN = "login"
    }
}
