package me.danlowe.meshcommunicator.features.db.messages

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import me.danlowe.meshcommunicator.features.nearby.data.NearbyMessageResult

@Entity(tableName = "messages")
@TypeConverters(
    NearbyMessageResultConverter::class
)
data class MessageDto(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    @ColumnInfo(name = "uuid")
    val uuid: String,
    @ColumnInfo(name = "originUserId")
    val originUserId: String,
    @ColumnInfo(name = "message")
    val message: String,
    @ColumnInfo(name = "timeSent")
    val timeSent: Long,
    @ColumnInfo(name = "timeReceived")
    val timeReceived: Long,
    @ColumnInfo(name = "sendState")
    val sendState: NearbyMessageResult
)