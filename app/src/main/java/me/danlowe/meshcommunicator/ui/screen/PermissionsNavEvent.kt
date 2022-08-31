package me.danlowe.meshcommunicator.ui.screen

sealed class PermissionsNavEvent {

    object PermissionsGranted : PermissionsNavEvent()

}