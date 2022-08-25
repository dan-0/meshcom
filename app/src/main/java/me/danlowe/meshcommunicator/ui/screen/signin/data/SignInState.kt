package me.danlowe.meshcommunicator.ui.screen.signin.data

sealed class SignInState {

    object ValidName : SignInState()

    object InvalidName : SignInState()

}