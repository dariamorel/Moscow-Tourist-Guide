package com.example.touristguide.domain.entities

import java.time.LocalDateTime
import java.time.LocalTime

data class Route(
    val id: Int,
    val order: List<Sight>,
    val orderPassed: List<Sight>,
    val totalTime: LocalTime,
    val date: LocalDateTime,
    val status: Status
): Comparable<Route> {
    enum class Status(val title: String) {
        IN_PROGRESS("В процессе"), PASSED("Завершенный"), DECLINED("Отмененный"), NOT_STARTED("Не начатый")
    }

    override fun compareTo(other: Route): Int {
        val compareList = listOf(Status.IN_PROGRESS, Status.NOT_STARTED, Status.PASSED, Status.DECLINED)
        val i = compareList.indexOf(status)
        val j = compareList.indexOf(other.status)
        return if (i < j) -1 else if (i > j) 1 else 0
    }
}
