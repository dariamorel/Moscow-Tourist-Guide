package com.example.touristguide.domain.entities

import org.osmdroid.util.GeoPoint

data class Sight(
    val id: Long,
    val title: String,
    val address: String,
    val timetable: String,
    val phone: String,
    val description: String,
    val bodyText: String,
    val url: String,
    val coords: GeoPoint,
    val subway: String,
    val images: List<String>,
    val categories: List<String>
)