package me.danlowe.meshcommunicator.features.nearby

sealed class AwaitingMessageState {

    object Created : AwaitingMessageState()

    object Success : AwaitingMessageState()

    object Fail : AwaitingMessageState()

}
