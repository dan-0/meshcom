package me.danlowe.meshcommunicator.ui.screen.conversation

import androidx.datastore.core.DataStore
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import me.danlowe.meshcommunicator.AppSettings
import me.danlowe.meshcommunicator.features.db.messages.MessagesDao
import me.danlowe.meshcommunicator.features.dispatchers.DispatcherProvider
import me.danlowe.meshcommunicator.features.dispatchers.buildHandledIoContext
import me.danlowe.meshcommunicator.features.nearby.NearbyConnections
import me.danlowe.meshcommunicator.features.nearby.data.ExternalUserId
import me.danlowe.meshcommunicator.nav.AppDestinations
import me.danlowe.meshcommunicator.util.ext.asMilliToInstant
import me.danlowe.meshcommunicator.util.ext.getMutableStateFlow
import me.danlowe.meshcommunicator.util.ext.launchInContext
import me.danlowe.meshcommunicator.util.ext.toIso8601String
import me.danlowe.meshcommunicator.util.helper.time.TimeFormatter
import timber.log.Timber
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ConversationViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    dispatchers: DispatcherProvider,
    private val messagesDao: MessagesDao,
    private val nearbyConnections: NearbyConnections,
    private val appSettings: DataStore<AppSettings>,
    private val timeFormatter: TimeFormatter
) : ViewModel() {

    private val externalUserId = savedStateHandle.get<String>(
        AppDestinations.Conversation.PARAM_EXTERNAL_USER_ID
    )!!.let {
        ExternalUserId(it)
    }

    private val _state = savedStateHandle.getMutableStateFlow<ConversationState>(
        key = "ConversationViewModelState",
        defaultValue = ConversationState.Loading
    )
    val state: Flow<ConversationState> = _state

    private val messageContext = dispatchers.buildHandledIoContext {
        // TODO Trigger event to notify user of error
    }

    private val dbContext = dispatchers.buildHandledIoContext {
        // Stub
    }

    init {
        launchInContext(dbContext) {

            val settings = appSettings.data.first()

            messagesDao.getAllAsFlow().collect { messages ->

                val messageData = messages.map { dto ->
                    MessageData(
                        originUserId = ExternalUserId(dto.originUserId),
                        message = dto.message,
                        timeSent = timeFormatter.instantToMediumLocalizedDateTime(
                            dto.timeSent.asMilliToInstant()
                        ),
                        timeReceived = dto.timeReceived.toIso8601String(),
                        isFromLocalUser = settings.userId == dto.originUserId
                    )
                }

                _state.value = ConversationState.Content(messageData)
            }
        }
    }

    fun sendMessage(message: String) {

        launchInContext(messageContext) {
            nearbyConnections.sendMessage(
                externalUserId = externalUserId,
                message = message,
                messageId = UUID.randomUUID()
            ).collect {
                Timber.d("Result $it")
            }
        }

    }

}