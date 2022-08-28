package me.danlowe.meshcommunicator.features.db.messages

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageDto(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    @ColumnInfo(name = "uuid")
    val uuid: String,
    @ColumnInfo(name = "conversationId")
    val conversationId: String,
    @ColumnInfo(name = "originUserId")
    val originUserId: String,
    @ColumnInfo(name = "message")
    val message: String,
    @ColumnInfo(name = "timeSent")
    val timeSent: Long,
    @ColumnInfo(name = "timeReceived")
    val timeReceived: Long,
)