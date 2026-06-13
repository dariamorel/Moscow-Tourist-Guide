package com.example.touristguide.data.dto

import com.google.gson.annotations.SerializedName

data class SightDto(
    val id: Int,
    val title: String,
    val img: String,
    val lat: Double,
    val lon: Double,
    val tags: List<String>? = null,
    @SerializedName("short_desc")
    val shortDesc: String,
    val desc: String,
    val timetable: String? = null,
    val addr: String? = null,
    val phone: String? = null,
    val subway: String? = null
)

data class RouteDto(
    val id: Int,
    @SerializedName("user_id")
    val userId: Int,
    val name: String? = null,
    val order: List<Int>? = null,
    @SerializedName("planned_order")
    val plannedOrder: List<SightDto>? = null,
    @SerializedName("total_time")
    val totalTime: String? = null,
    val date: String,
    val status: String? = null
)

data class RouteInputDto(
    @SerializedName("user_id")
    val userId: Int,
    val name: String? = null,
    @SerializedName("planned_order")
    val plannedOrder: List<Int>
)

data class RouteUpdateInputDto(
    val name: String? = null,
    val order: List<Int>? = null,
    @SerializedName("planned_order")
    val plannedOrder: List<Int>? = null,
    val status: String? = null
)

data class OptimizeRouteInputDto(
    @SerializedName("sight_ids")
    val sightIds: List<Int>
)

data class OptimizeRouteResponseDto(
    @SerializedName("optimized_order")
    val optimizedOrder: List<SightDto>,
    @SerializedName("total_time")
    val totalTime: String
)

data class UserDto(
    val id: Int,
    val login: String,
    @SerializedName("created_at")
    val createdAt: String
)

data class UserInputDto(
    val login: String,
    val password: String
)

data class LoginResponseDto(
    val token: String,
    val user: UserDto
)

data class AchievementDto(
    val id: Int,
    val type: String,
    val name: String,
    val description: String,
    @SerializedName("icon_url")
    val iconUrl: String,
    @SerializedName("required_count")
    val requiredCount: Int,
    val tag: String? = null,
    val progress: Int = 0
)
