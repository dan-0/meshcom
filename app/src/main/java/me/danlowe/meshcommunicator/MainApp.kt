package me.danlowe.meshcommunicator

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class MainApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Logging.init()
        Timber.d("App start")
    }
}