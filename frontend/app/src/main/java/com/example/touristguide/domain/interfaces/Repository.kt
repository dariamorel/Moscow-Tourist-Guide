package com.example.touristguide.domain.interfaces

import com.example.touristguide.domain.entities.Achievement
import com.example.touristguide.domain.entities.Route
import com.example.touristguide.domain.entities.Sight
import kotlinx.coroutines.flow.StateFlow

interface Repository {
    val allRoutes: StateFlow<List<Route>>

    /**
     * Получить сайт по id.
     */
    suspend fun getSight(id: Int): Sight

    /**
     * Получить список всех сайтов.
     */
    suspend fun getAllSights(): List<Sight>

    /**
     * Получить список маршрутов по userId.
     */
//    suspend fun getAllRoutes(): List<Route>

    /**
     * Добавить новый маршрут.
     */
    suspend fun addNewRoute(order: List<Sight>)

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
     * Зарегистрироваться.
     */
    suspend fun registrate()

    /**
     * Залогиниться.
     */
    suspend fun login()

    /**
     * получить список всех категорий.
     */
    suspend fun getAllCategories(): List<String>
}
