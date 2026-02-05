package com.otus.dihomework

import android.app.Application

class ProductsApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ServiceLocator.init(this)
    }
}
