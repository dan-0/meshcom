package me.danlowe.meshcommunicator.features.appsettings

import kotlinx.coroutines.flow.StateFlow
import me.danlowe.meshcommunicator.AppSettings

/**
 * Allows app settings to be retrieved as a StateFlow
 */
interface LiveAppSettings {

    val appSettings: StateFlow<AppSettings>

}
