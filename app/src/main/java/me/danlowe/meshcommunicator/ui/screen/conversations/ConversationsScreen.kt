package me.danlowe.meshcommunicator.ui.screen.conversations

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.parcelize.Parcelize
import me.danlowe.meshcommunicator.features.dispatchers.DispatcherProvider
import me.danlowe.meshcommunicator.util.ext.getMutableStateFlow
import javax.inject.Inject

@Composable
fun ConversationsScreen() {

}

@HiltViewModel
class ConversationsViewModel @Inject constructor(
    dispatcherProvider: DispatcherProvider,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _state = savedStateHandle.getMutableStateFlow<ConversationsState>(
        key = "ConversationsViewModelState",
        defaultValue = ConversationsState.Loading
    )
    val state: Flow<ConversationsState> = _state

}

@Parcelize
data class ConversationInfo(
    val userName: String,
    val userId: String,
    val lastSeen: String,
    val lastMessage: String?
) : Parcelable

sealed class ConversationsState : Parcelable {

    @Parcelize
    object Loading : ConversationsState()

    @Parcelize
    data class Content(
        val conversations: ConversationInfo
    ) : ConversationsState()

}