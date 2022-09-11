package me.danlowe.meshcommunicator.ui.screen.splash

import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import me.danlowe.meshcommunicator.AppSettings
import me.danlowe.meshcommunicator.features.dispatchers.DispatcherProvider
import me.danlowe.meshcommunicator.features.dispatchers.buildHandledIoContext
import me.danlowe.meshcommunicator.ui.screen.splash.data.SplashEvent
import me.danlowe.meshcommunicator.ui.screen.splash.data.SplashState
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val dataStore: DataStore<AppSettings>,
    dispatchers: DispatcherProvider
) : ViewModel() {

    private val _state = MutableStateFlow<SplashState>(SplashState.Loading)
    val state: Flow<SplashState> = _state

    private val _event = MutableSharedFlow<SplashEvent>(
        1,
        0,
        BufferOverflow.DROP_LATEST
    )
    val event: Flow<SplashEvent> = _event

    private val dataStoreContext = dispatchers.buildHandledIoContext {
        _state.value = SplashState.Error
    }

    init {
        loadCredentials()
    }

    fun loadCredentials() {
        viewModelScope.launch(dataStoreContext) {
            _state.value = SplashState.Loading

            val settings = dataStore.data.firstOrNull()

            settings ?: run {
                _event.tryEmit(SplashEvent.NoCredentials)
                return@launch
            }

            with(settings) {
                val newEvent = if (userId.isNullOrEmpty() || userName.isNullOrEmpty()) {
                    SplashEvent.NoCredentials
                } else {
                    SplashEvent.HasCredentials
                }

                _event.tryEmit(newEvent)
            }
        }
    }

}