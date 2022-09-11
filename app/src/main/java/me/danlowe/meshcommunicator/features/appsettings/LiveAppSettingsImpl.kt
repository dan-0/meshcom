package me.danlowe.meshcommunicator.features.appsettings

import androidx.datastore.core.DataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import me.danlowe.meshcommunicator.AppSettings
import me.danlowe.meshcommunicator.features.dispatchers.DispatcherProvider
import javax.inject.Inject

/**
 * Retrieves [AppSettings] from the proto datastore
 */
class LiveAppSettingsImpl @Inject constructor(
    private val dataStore: DataStore<AppSettings>,
    dispatchers: DispatcherProvider
) : LiveAppSettings {

    private val scope = CoroutineScope(dispatchers.io)

    private val _appSettings = MutableStateFlow<AppSettings>(AppSettings.getDefaultInstance())
    override val appSettings: StateFlow<AppSettings> = _appSettings

    init {
        scope.launch {
            dataStore.data.collect {
                _appSettings.value = it
            }
        }
    }

}
