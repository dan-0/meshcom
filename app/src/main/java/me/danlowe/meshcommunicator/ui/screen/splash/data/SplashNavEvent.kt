package me.danlowe.meshcommunicator.ui.screen.splash.data

sealed class SplashNavEvent {

    object NoCredentials : SplashNavEvent()

    object HasCredentials : SplashNavEvent()

}
