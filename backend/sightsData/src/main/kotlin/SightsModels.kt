package org.example

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SightsResponse(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<Sight>
)

@Serializable
data class Sight(
    val id: Int,
    val title: String,
    val address: String? = null,
    val timetable: String? = null,
    val phone: String? = null,
    val description: String? = null,
    @SerialName("body_text") val bodyText: String? = null,
    @SerialName("foreign_url") val foreignUrl: String? = null,
    val coords: Coords? = null,
    val subway: String? = null,
    val images: List<SightImage> = emptyList(),
    val categories: List<String> = emptyList()
)

@Serializable
data class Coords(
    val lat: Double?,
    val lon: Double?
)

@Serializable
data class SightImage(
    val image: String,
    val source: ImageSource? = null
)

@Serializable
data class ImageSource(
    val name: String? = null,
    val link: String? = null
)

