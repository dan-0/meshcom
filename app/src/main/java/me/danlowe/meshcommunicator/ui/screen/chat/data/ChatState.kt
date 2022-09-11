package me.danlowe.meshcommunicator.ui.screen.chat.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class ChatState : Parcelable {

    @Parcelize
    object Loading : ChatState()

    @Parcelize
    object Error : ChatState()

    @Parcelize
    data class Content(
        val messages: List<ChatData>
    ) : ChatState()

}