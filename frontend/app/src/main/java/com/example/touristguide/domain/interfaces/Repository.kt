package com.example.touristguide.domain.interfaces

import com.example.touristguide.domain.entities.Achievement
import com.example.touristguide.domain.entities.OptimizedSightsOrder
import com.example.touristguide.domain.entities.Route
import com.example.touristguide.domain.entities.Sight
import kotlinx.coroutines.flow.StateFlow

interface Repository {

    /**
     * Получить список всех сайтов.
     */
    suspend fun getAllSights(limit: Int? = null, offset: Int? = null, tag: String? = null): List<Sight>

    /**
     * Получить список маршрутов по userId.
     */
    suspend fun getAllRoutes(): List<Route>

    /**
     * Добавить новый маршрут.
     */
    suspend fun addNewRoute(planned: List<Sight>, name: String)

    /**
     * Обновить данные о маршруте.
     */
    suspend fun updateRoute(routeId: Int, newRoute: Route)

    /**
     * Получить оптимизированный маршрут.
     */
    suspend fun optimizeRoute(sights: List<Int>): OptimizedSightsOrder

    /**
     * Получить список возможных достижений.
     */
    suspend fun getAllAchievements(): List<Achievement>

    suspend fun register(login: String, password: String)

    suspend fun login(login: String, password: String)

    fun isLogin(): Boolean

    fun getUserId(): Int?

    fun logout()

    fun getLogin(): String?

    companion object {
        const val REPOSITORY_TAG = "Sights Repository Tag"
    }
}
