package com.example.touristguide.domain.entities

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.osmdroid.util.GeoPoint

@Parcelize
data class Sight(
    val id: Int,
    val image: String,
    val title: String,
    val address: String? = null,
    val timetable: String? = null,
    val phone: String? = null,
    val description: String,
    val bodyText: String,
    val coords: GeoPoint,
    val subway: String? = null,
    val categories: List<String> = emptyList()
): Parcelable