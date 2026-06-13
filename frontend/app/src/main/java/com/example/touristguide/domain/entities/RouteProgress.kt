package com.example.touristguide.domain.entities

fun Route.passedSightsCount(): Int =
    planned.count { isPassed[it.id] == true }

fun Route.progressPercent(): Int {
    if (planned.isEmpty()) return 0
    return passedSightsCount() * 100 / planned.size
}

fun Route.progressFraction(): Float {
    if (planned.isEmpty()) return 0f
    return passedSightsCount().toFloat() / planned.size
}

fun Route.isFullyPassed(): Boolean =
    planned.isNotEmpty() && planned.all { isPassed[it.id] == true }
