package me.danlowe.meshcommunicator.ui.screen.splash.data

sealed class SplashState {

    object Loading : SplashState()

    object Error : SplashState()

}
