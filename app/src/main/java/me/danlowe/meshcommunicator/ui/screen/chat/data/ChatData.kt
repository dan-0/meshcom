package me.danlowe.meshcommunicator.ui.screen.chat.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import me.danlowe.meshcommunicator.features.nearby.data.ExternalUserId

sealed class ChatData : Parcelable {

    @Parcelize
    data class ReceivedChat(
        val originUserId: ExternalUserId,
        val message: String,
        val timeSent: String,
        val timeReceived: String,
    ) : ChatData()

    @Parcelize
    data class SentChat(
        val message: String,
        val timeSent: String,
        val timeReceived: String,
    ) : ChatData()

}