package me.danlowe.meshcommunicator.ui.screen.conversations.data

import me.danlowe.meshcommunicator.features.nearby.data.ExternalUserId

sealed class ConversationsNavEvent {

    data class OpenConversation(
        val userName: String,
        val externalUserId: ExternalUserId
    ) : ConversationsNavEvent()

}