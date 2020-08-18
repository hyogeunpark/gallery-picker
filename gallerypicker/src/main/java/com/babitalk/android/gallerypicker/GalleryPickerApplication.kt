package com.babitalk.android.gallerypicker

import android.app.Application
import android.content.Context

class GalleryPickerApplication: Application() {
    companion object {
        lateinit var sContext: Context
    }

    override fun onCreate() {
        super.onCreate()
        sContext = this.applicationContext
    }
}