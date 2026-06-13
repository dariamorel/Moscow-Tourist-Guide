package com.example.touristguide.domain.entities

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.OffsetDateTime

@Parcelize
data class OptimizedSightsOrder(
    val sights: List<Sight> = emptyList(),
    val totalTime: String = ""
): Parcelable