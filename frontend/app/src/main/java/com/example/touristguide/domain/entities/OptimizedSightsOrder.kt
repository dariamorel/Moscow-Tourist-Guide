package com.example.touristguide.domain.entities

import java.time.OffsetDateTime

data class OptimizedSightsOrder(
    val sights: List<Sight>,
    val totalTime: String
)