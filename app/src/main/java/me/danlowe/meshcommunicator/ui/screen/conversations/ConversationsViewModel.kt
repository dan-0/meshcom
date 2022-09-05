package me.danlowe.meshcommunicator.ui.screen.conversations

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import me.danlowe.meshcommunicator.features.db.conversations.ContactDto
import me.danlowe.meshcommunicator.features.db.conversations.ContactsDao
import me.danlowe.meshcommunicator.features.dispatchers.DispatcherProvider
import me.danlowe.meshcommunicator.features.dispatchers.buildHandledIoContext
import me.danlowe.meshcommunicator.features.nearby.NearbyConnections
import me.danlowe.meshcommunicator.ui.screen.conversations.data.ConversationInfo
import me.danlowe.meshcommunicator.ui.screen.conversations.data.ConversationsState
import me.danlowe.meshcommunicator.util.ext.getMutableStateFlow
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class ConversationsViewModel @Inject constructor(
    dispatcherProvider: DispatcherProvider,
    savedStateHandle: SavedStateHandle,
    contactsDao: ContactsDao,
    private val nearbyConnections: NearbyConnections
) : ViewModel() {

    private val _state = savedStateHandle.getMutableStateFlow<ConversationsState>(
        key = "ConversationsViewModelState",
        defaultValue = ConversationsState.Loading
    )
    val state: Flow<ConversationsState> = _state

    private val dbContext = dispatcherProvider.buildHandledIoContext {
        // TODO handle error
    }

    init {
        nearbyConnections.start()

        viewModelScope.launch(dbContext) {
            contactsDao.getAllAsFlow().collect { contacts ->
                mapContactDtoToState(contacts)
            }
        }
    }

    private fun mapContactDtoToState(contacts: List<ContactDto>) {
        contacts.map { contact ->
            ConversationInfo(
                userName = contact.userName,
                userId = contact.userId,
                lastSeen = Instant.ofEpochMilli(contact.lastSeen).toString(),
                lastMessage = null
            )
        }.also { conversations ->
            _state.value = ConversationsState.Content(conversations)
        }
    }

    override fun onCleared() {
        super.onCleared()
        nearbyConnections.stop()
    }
}