package com.linguacards.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class LinguaCardsApplication : Application() {

    override fun onCreate() {
        super.onCreate()
    }
}