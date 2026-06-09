package com.example.touristguide.presentation.di

object PresentationComponentHolder {
    lateinit var component: PresentationComponent
        private set

    fun init(dependencies: PresentationComponent.Dependencies) {
        component = PresentationComponent(dependencies)
    }
}