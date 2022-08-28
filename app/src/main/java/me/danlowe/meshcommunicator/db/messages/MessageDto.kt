package me.danlowe.meshcommunicator.db.messages

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageDto(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long,
    val conversationId: String,
    val originUserId: String,
    val message: String,
    val timeSent: String,
    val timeReceived: String,
)