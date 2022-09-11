package me.danlowe.meshcommunicator.ui.screen.signin

import androidx.datastore.core.DataStore
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import me.danlowe.meshcommunicator.AppSettings
import me.danlowe.meshcommunicator.features.dispatchers.DispatcherProvider
import me.danlowe.meshcommunicator.features.dispatchers.buildHandledIoContext
import me.danlowe.meshcommunicator.ui.screen.signin.data.SignInEvent
import me.danlowe.meshcommunicator.ui.screen.signin.data.SignInState
import me.danlowe.meshcommunicator.util.ext.EventFlow
import me.danlowe.meshcommunicator.util.ext.getMutableStateFlow
import me.danlowe.meshcommunicator.util.ext.launchInContext
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val settingsDataStore: DataStore<AppSettings>,
    dispatchers: DispatcherProvider,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _event = EventFlow<SignInEvent>()
    val events: Flow<SignInEvent> = _event

    private val _state = savedStateHandle.getMutableStateFlow<SignInState>(
        "SignInViewModelState",
        SignInState.ValidName
    )
    val state: Flow<SignInState> = _state

    private val ioContext = dispatchers.buildHandledIoContext {
        _state.value = SignInState.Error
    }

    fun signIn(rawUserName: String) {

        val userName = rawUserName.trim()

        if (isUsernameValid(userName)) {
            _state.value = SignInState.ValidName
        } else {
            _state.value = SignInState.InvalidName
            return
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

    private fun isUsernameValid(userName: String) = userName.length in VALID_USERNAME_SIZE_RANGE

    companion object {

        val VALID_USERNAME_SIZE_RANGE = 5..55

    }

}

