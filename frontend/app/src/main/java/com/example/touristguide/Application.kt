package com.example.touristguide

import android.app.Application
import com.example.touristguide.data.di.DataComponentHolder
import com.example.touristguide.domain.interfaces.Repository
import com.example.touristguide.presentation.di.PresentationComponent
import com.example.touristguide.presentation.di.PresentationComponentHolder

class Application : Application() {

    override fun onCreate() {
        super.onCreate()
        DataComponentHolder.init(applicationContext)
        PresentationComponentHolder.init(object : PresentationComponent.Dependencies{
            override fun getRepository(): Repository {
                return DataComponentHolder.component.repository
            }
        })
    }
}