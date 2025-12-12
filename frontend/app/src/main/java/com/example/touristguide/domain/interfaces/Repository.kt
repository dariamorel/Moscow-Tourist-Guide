package com.example.touristguide.domain.interfaces

import com.example.touristguide.domain.entities.Sight

interface Repository {
    suspend fun getAllSights(): List<Sight>
    suspend fun getCategoriesList(): List<String>
}
