package me.danlowe.meshcommunicator.ui.screen.conversations

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ConversationInfo(
    val userName: String,
    val userId: String,
    val lastSeen: String,
    val lastMessage: String? = null
) : Parcelable