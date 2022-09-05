package me.danlowe.meshcommunicator.ui.screen.conversation

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class ConversationState : Parcelable {

    @Parcelize
    object Loading : ConversationState()

    @Parcelize
    object Error : ConversationState()

    @Parcelize
    data class Content(
        val messages: List<MessageData>
    ) : ConversationState()

}