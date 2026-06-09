package com.example.touristguide.data.di

import com.example.touristguide.data.repository.TestRepository
import com.example.touristguide.domain.interfaces.Repository

class DataComponent {
    val repository by lazy { createRepository() }

    fun createRepository(): Repository {
        return TestRepository()
    }
}