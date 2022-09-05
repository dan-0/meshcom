package me.danlowe.meshcommunicator.ui.screen.conversations.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class ConversationsState : Parcelable {

    @Parcelize
    object Loading : ConversationsState()

    @Parcelize
    data class Content(
        val conversations: List<ConversationInfo>
    ) : ConversationsState()

}