package me.danlowe.meshcommunicator.ui.screen.splash.data

sealed class SplashEvent {

    object HasCredentials : SplashEvent()

    object NoCredentials : SplashEvent()

}