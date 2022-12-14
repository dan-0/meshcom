package me.danlowe.meshcommunicator.ui.screen.conversations

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.danlowe.meshcommunicator.features.db.conversations.ContactDto
import me.danlowe.meshcommunicator.features.db.conversations.ContactsDao
import me.danlowe.meshcommunicator.features.db.messages.MessagesDao
import me.danlowe.meshcommunicator.features.dispatchers.DispatcherProvider
import me.danlowe.meshcommunicator.features.dispatchers.buildHandledIoContext
import me.danlowe.meshcommunicator.features.nearby.AppConnectionHandler
import me.danlowe.meshcommunicator.features.nearby.data.ExternalUserId
import me.danlowe.meshcommunicator.ui.screen.conversations.data.ConversationConnectionState
import me.danlowe.meshcommunicator.ui.screen.conversations.data.ConversationInfo
import me.danlowe.meshcommunicator.ui.screen.conversations.data.ConversationsState
import me.danlowe.meshcommunicator.util.ext.asMilliToInstant
import me.danlowe.meshcommunicator.util.ext.getMutableStateFlow
import me.danlowe.meshcommunicator.util.helper.time.TimeFormatter
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ConversationsViewModel @Inject constructor(
    dispatchers: DispatcherProvider,
    savedStateHandle: SavedStateHandle,
    contactsDao: ContactsDao,
    private val messagesDao: MessagesDao,
    private val appConnectionHandler: AppConnectionHandler,
    private val timeFormatter: TimeFormatter,
) : ViewModel() {

    private val _state = savedStateHandle.getMutableStateFlow<ConversationsState>(
        key = "ConversationsViewModelState",
        defaultValue = ConversationsState.Loading
    )
    val state: Flow<ConversationsState> = _state

    private val dbContext = dispatchers.buildHandledIoContext {
        // TODO handle error
    }

    init {
        appConnectionHandler.start()

        viewModelScope.launch(dbContext) {
            combine(
                contactsDao.getAllAsFlow(),
                appConnectionHandler.activeConnectionsState,
            ) { contacts, connectionStates ->
                mapContactDtoToState(contacts, connectionStates)
            }
                .stateIn(CoroutineScope(dispatchers.io), SharingStarted.Eagerly, listOf())
                .collect { conversations ->
                    Timber.d("collecting")
                    _state.value = ConversationsState.Content(conversations)
                }
        }
    }

    private suspend fun mapContactDtoToState(
        contacts: List<ContactDto>,
        connectedIds: Set<ExternalUserId>
    ): List<ConversationInfo> {
        Timber.d("Mapping contacts, ids, $contacts $connectedIds")
        return contacts.map { contact ->
            val lastMessage = coroutineScope {
                async {
                    messagesDao.getLastMessageByExternalUserId(contact.externalUserId)
                }
            }

            val externalUserId = ExternalUserId(contact.externalUserId)

            val connectionState = if (connectedIds.contains(externalUserId)) {
                ConversationConnectionState.Connected
            } else {
                ConversationConnectionState.NotConnected
            }

            Timber.d("Connection state: $connectionState")

            ConversationInfo(
                userName = contact.userName,
                userId = contact.externalUserId,
                lastSeen = timeFormatter.instantToMediumLocalizedDateTime(contact.lastSeen.asMilliToInstant()),
                connectionState = connectionState,
                lastMessage = lastMessage.await()
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        appConnectionHandler.stop()
    }
}
