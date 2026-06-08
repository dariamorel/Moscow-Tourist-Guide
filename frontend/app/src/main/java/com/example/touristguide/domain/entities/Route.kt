package com.example.touristguide.domain.entities

import java.time.OffsetDateTime

data class Route(
    val id: Int = 0,
    val isPassed: Map<Int, Boolean> = emptyMap(),
    val planned: List<Sight> = emptyList(),
    val totalTime: String? = null,
    val date: OffsetDateTime? = null,
    val status: Status = Status.NOT_STARTED
): Comparable<Route> {
    enum class Status(val title: String) {
        IN_PROGRESS("in_progress"), PASSED("completed"), DECLINED("declined"), NOT_STARTED("planned")
    }

    override fun compareTo(other: Route): Int {
        val compareList = listOf(Status.IN_PROGRESS, Status.NOT_STARTED, Status.PASSED, Status.DECLINED)
        val i = compareList.indexOf(status)
        val j = compareList.indexOf(other.status)
        return if (i < j) -1 else if (i > j) 1 else 0
    }
}
