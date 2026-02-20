package com.example.touristguide.domain.entities

import java.time.LocalDateTime
import java.time.LocalTime

data class Route(
    val id: Int,
    val order: List<Int>,
    val orderPassed: List<Int>,
    val totalTime: LocalTime,
    val date: LocalDateTime,
    val status: Status
) {
    enum class Status {
        IN_PROGRESS, PASSED, DECLINED, NOT_STARTED
    }
}
