package me.danlowe.meshcommunicator.features.db.messages

import androidx.room.*
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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(messageDto: MessageDto)

    @Update
    suspend fun update(messageDto: MessageDto)

    @Query("SELECT * FROM messages where uuid = :messageId")
    suspend fun getByUuid(messageId: String): MessageDto?

    @Query(
        "SELECT message FROM messages " +
                "WHERE originUserId = :externalUserId " +
                "ORDER BY timeSent " +
                "DESC LIMIT 1"
    )
    suspend fun getLastMessageByExternalUserId(externalUserId: String): String?

    @Query(
        "SELECT * from messages " +
                "WHERE targetUserId = :externalUserId " +
                "AND sendState != 3"

    )
    suspend fun getUnsentMessagesFromUser(externalUserId: String): List<MessageDto>

}