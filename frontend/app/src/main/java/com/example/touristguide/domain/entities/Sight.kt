package com.example.touristguide.domain.entities

data class Sight(
    val id: Long,
    val title: String,
    val address: String,
    val timetable: String,
    val phone: String,
    val description: String,
    val bodyText: String,
    val url: String,
    val coords: Coords,
    val subway: String,
    val images: List<String>,
    val categories: List<String>
)

data class Coords(
    val latitude: Double,
    val longitude: Double
)
