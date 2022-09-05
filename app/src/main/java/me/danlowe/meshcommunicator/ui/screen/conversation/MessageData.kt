package me.danlowe.meshcommunicator.ui.screen.conversation

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import me.danlowe.meshcommunicator.features.nearby.data.ExternalUserId

@Parcelize
data class MessageData(
    val originUserId: ExternalUserId,
    val message: String,
    val timeSent: String,
    val timeReceived: String
) : Parcelable