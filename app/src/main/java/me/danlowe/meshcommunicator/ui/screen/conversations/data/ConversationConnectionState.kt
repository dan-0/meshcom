package me.danlowe.meshcommunicator.ui.screen.conversations.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class ConversationConnectionState : Parcelable {

    @Parcelize
    object Connected : ConversationConnectionState()

    @Parcelize
    object NotConnected : ConversationConnectionState()

}