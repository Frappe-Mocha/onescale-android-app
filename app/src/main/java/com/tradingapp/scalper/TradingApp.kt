package com.tradingapp.scalper

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TradingApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize app-level components here
    }
}
