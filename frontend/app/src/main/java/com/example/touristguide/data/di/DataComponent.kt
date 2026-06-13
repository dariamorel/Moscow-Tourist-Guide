package com.example.touristguide.data.di

import android.content.Context
import com.example.touristguide.data.repository.RepositoryImpl
import com.example.touristguide.data.service.DataService
import com.example.touristguide.domain.interfaces.Repository

class DataComponent(
    context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    val dataService: DataService by lazy { DataService.create() }

    val repository: Repository by lazy { RepositoryImpl(prefs, dataService) }

    companion object {
        private const val PREFS_NAME = "user_prefs"
    }
}
