package com.example.touristguide.domain.entities

data class Achievement(
    val id: Int,
    val type: Type,
    val name: String,
    val description: String,
    val iconUrl: String,
    val requiredCount: Int,
    val tag: String
) {
    enum class Type(type: String) {
        SIGHTS_VISITED("sights_visited"), ROUTES_COMPLETED("routes_completed"),
        TAG_COLLECTOR("tag_collector")
    }
}
