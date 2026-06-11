package com.example.touristguide.domain.interfaces

import com.example.touristguide.domain.entities.Achievement
import com.example.touristguide.domain.entities.OptimizedSightsOrder
import com.example.touristguide.domain.entities.Route
import com.example.touristguide.domain.entities.Sight
import kotlinx.coroutines.flow.StateFlow

interface Repository {

    /**
     * Получить сайт по id.
     */
    suspend fun getSight(sightId: Int): Sight

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
     * Получить список пройденных маршрутов.
     */
    suspend fun getPassedRoutes(): List<Route>

    /**
     * Получить список отмененных маршрутов.
     */
    suspend fun getDeclinedRoutes(): List<Route>

    /**
     * Получить список маршрутов в процессе прохождения.
     */
    suspend fun getRoutesInProgress(): List<Route>

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

    /**
     * Получиьт список полученных достижений.
     */
    suspend fun getCompletedAchievements(): List<Achievement>

    /**
     * Обновить достижение.
     */
    suspend fun updateAchievement(achievementId: Int, newAchievement: Achievement)

    /**
     * получить список всех категорий.
     */
    suspend fun getAllCategories(): List<String>

    companion object {
        const val REPOSITORY_TAG = "Sights Repository Tag"
    }
}
