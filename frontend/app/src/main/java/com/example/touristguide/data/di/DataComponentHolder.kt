package com.example.touristguide.data.di

object DataComponentHolder {
    lateinit var component: DataComponent
        private set

    fun init() {
        component = DataComponent()
    }
}