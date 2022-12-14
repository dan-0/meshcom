package me.danlowe.meshcommunicator.ui.screen.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import me.danlowe.meshcommunicator.features.appsettings.LiveAppSettings
import me.danlowe.meshcommunicator.features.db.messages.MessageDto
import me.danlowe.meshcommunicator.features.db.messages.MessagesDao
import me.danlowe.meshcommunicator.features.dispatchers.DispatcherProvider
import me.danlowe.meshcommunicator.features.dispatchers.buildHandledIoContext
import me.danlowe.meshcommunicator.features.nearby.AppConnectionHandler
import me.danlowe.meshcommunicator.features.nearby.data.ExternalUserId
import me.danlowe.meshcommunicator.nav.AppDestinations
import me.danlowe.meshcommunicator.ui.screen.chat.data.ChatData
import me.danlowe.meshcommunicator.ui.screen.chat.data.ChatState
import me.danlowe.meshcommunicator.util.ext.asMilliToInstant
import me.danlowe.meshcommunicator.util.ext.getMutableStateFlow
import me.danlowe.meshcommunicator.util.ext.launchInContext
import me.danlowe.meshcommunicator.util.ext.toIso8601String
import me.danlowe.meshcommunicator.util.helper.time.TimeFormatter
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    dispatchers: DispatcherProvider,
    private val messagesDao: MessagesDao,
    private val appConnectionHandler: AppConnectionHandler,
    private val appSettings: LiveAppSettings,
    private val timeFormatter: TimeFormatter
) : ViewModel() {

    private val externalUserId = savedStateHandle.get<String>(
        AppDestinations.Chat.PARAM_EXTERNAL_USER_ID
    )!!.let {
        ExternalUserId(it)
    }

    private val _state = savedStateHandle.getMutableStateFlow<ChatState>(
        key = "ConversationViewModelState",
        defaultValue = ChatState.Loading
    )
    val state: Flow<ChatState> = _state

    private val messageContext = dispatchers.buildHandledIoContext {
        // Stub,
    }

    private val dbContext = dispatchers.buildHandledIoContext {
        _state.value = ChatState.Error
    }

    private var messagesJob: Job? = null

    init {
        observeMessages()
    }

    fun observeMessages() {
        messagesJob?.cancel()
        messagesJob = launchInContext(dbContext) {

            val settings = appSettings.appSettings.value

            messagesDao.getAllAsFlow().collect { messages ->

                val messageData = messages.map { dto ->
                    if (settings.userId == dto.originUserId) {
                        ChatData.SentChat(
                            message = dto.message,
                            timeSent = timeFormatter.instantToMediumLocalizedDateTime(
                                dto.timeSent.asMilliToInstant()
                            ),
                            timeReceived = dto.timeReceived.let { timeReceived ->
                                if (timeReceived == MessageDto.NO_RECEIVED_TIME) {
                                    null
                                } else {
                                    timeReceived.toIso8601String()
                                }
                            }
                        )
                    } else {
                        ChatData.ReceivedChat(
                            originUserId = ExternalUserId(id = dto.originUserId),
                            message = dto.message,
                            timeSent = timeFormatter.instantToMediumLocalizedDateTime(
                                dto.timeSent.asMilliToInstant()
                            ),
                            timeReceived = dto.timeReceived.toIso8601String()
                        )
                    }
                }

                _state.value = ChatState.Content(messageData)
            }
        }
    }

    fun sendMessage(message: String) {

        launchInContext(messageContext) {
            appConnectionHandler.sendMessage(
                externalUserId = externalUserId,
                message = message,
                messageId = UUID.randomUUID()
            ).collect {
                Timber.d("Result $it")
            }
        }

    }

}
