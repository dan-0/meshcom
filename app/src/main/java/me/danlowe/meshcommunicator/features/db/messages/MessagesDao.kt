package me.danlowe.meshcommunicator.features.db.messages

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

/**
 * DAO for individual messages that have been sent and received
 */
@Dao
interface MessagesDao {

    @Query("SELECT * FROM messages")
    suspend fun getAll(): List<MessageDto>

    @Insert
    suspend fun insert(messageDto: MessageDto)

}