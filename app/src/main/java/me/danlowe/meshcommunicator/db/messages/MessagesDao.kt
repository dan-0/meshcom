package me.danlowe.meshcommunicator.db.messages

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

/**
 * DAO for individual messages that have been sent and received
 */
@Dao
interface MessagesDao {

    @Query("SELECT * FROM messages")
    fun getAll(): List<MessageDto>

    @Insert
    fun insert(messageDto: MessageDto)

}