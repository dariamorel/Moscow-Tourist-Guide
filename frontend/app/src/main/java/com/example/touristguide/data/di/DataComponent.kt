package com.example.touristguide.data.di

import android.content.Context
import com.example.touristguide.data.repository.TestRepository
import com.example.touristguide.domain.interfaces.Repository

class DataComponent(
    context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    val repository: Repository by lazy { TestRepository(prefs) }

    companion object {
        private const val PREFS_NAME = "user_prefs"
    }
}
