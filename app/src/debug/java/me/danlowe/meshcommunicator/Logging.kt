package me.danlowe.meshcommunicator

import timber.log.Timber

object Logging {

    fun init() {
        Timber.plant(Timber.DebugTree())
    }

}