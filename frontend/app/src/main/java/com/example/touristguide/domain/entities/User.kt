package com.example.touristguide.domain.entities

import java.time.OffsetDateTime

data class User(
    val id: Int,
    val login: String,
    val createdAt: OffsetDateTime
)
