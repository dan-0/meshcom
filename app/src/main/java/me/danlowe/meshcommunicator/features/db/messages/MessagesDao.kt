package me.danlowe.meshcommunicator.features.db.messages

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * DAO for individual messages that have been sent and received
 */
@Dao
interface MessagesDao {

    @Query("SELECT * FROM messages")
    suspend fun getAll(): List<MessageDto>

    @Query("SELECT * FROM messages")
    fun getAllAsFlow(): Flow<List<MessageDto>>

    @Insert
    suspend fun insert(messageDto: MessageDto)

    @Update
    suspend fun update(messageDto: MessageDto)

    @Query("SELECT * FROM messages where uuid = :messageId")
    suspend fun getByUuid(messageId: String): MessageDto?

}