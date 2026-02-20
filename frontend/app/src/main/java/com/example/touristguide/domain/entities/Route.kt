package com.example.touristguide.domain.entities

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDateTime
import java.time.LocalTime

data class Route(
    val id: Int = 0,
    val order: List<Sight> = emptyList(),
    val orderPassed: List<Sight> = emptyList(),
    val totalTime: LocalTime? = null,
    val date: LocalDateTime? = null,
    val status: Status = Status.NOT_STARTED
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
