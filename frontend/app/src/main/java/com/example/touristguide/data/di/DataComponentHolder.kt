package com.example.touristguide.data.di

import android.content.Context

object DataComponentHolder {
    lateinit var component: DataComponent
        private set

    fun init(context: Context) {
        component = DataComponent(context)
    }
}