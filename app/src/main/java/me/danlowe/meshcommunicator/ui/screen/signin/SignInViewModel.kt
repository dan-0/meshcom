package me.danlowe.meshcommunicator.ui.screen.signin

import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import me.danlowe.meshcommunicator.AppSettings
import me.danlowe.meshcommunicator.features.dispatchers.DispatcherProvider
import me.danlowe.meshcommunicator.features.dispatchers.buildHandledIoContext
import me.danlowe.meshcommunicator.ui.screen.signin.data.SignInEvent
import me.danlowe.meshcommunicator.ui.screen.signin.data.SignInState
import me.danlowe.meshcommunicator.util.ext.EventFlow
import me.danlowe.meshcommunicator.util.ext.launchInContext
import java.util.*
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val settingsDataStore: DataStore<AppSettings>,
    dispatchers: DispatcherProvider,
) : ViewModel() {

    private val ioContext = dispatchers.buildHandledIoContext { }

    private val _event = EventFlow<SignInEvent>()
    val events: Flow<SignInEvent> = _event

    private val _state = MutableStateFlow<SignInState>(SignInState.ValidName)
    val state: Flow<SignInState> = _state

    fun signIn(userName: String) {

        if (userName.length < 5 || userName.length > 55) {
            _state.value = SignInState.InvalidName
            return
        } else {
            _state.value = SignInState.ValidName
        }

        launchInContext(ioContext) {
            settingsDataStore.updateData { settings ->
                settings.toBuilder()
                    .setUserId(UUID.randomUUID().toString())
                    .setUserName(userName)
                    .build()
            }

            _event.emit(SignInEvent.Complete)
        }
    }

}

