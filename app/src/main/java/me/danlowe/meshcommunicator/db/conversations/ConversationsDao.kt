package me.danlowe.meshcommunicator.db.conversations

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

/**
 * DAO for conversations between different users
 */
@Dao
interface ConversationsDao {

    @Query("SELECT * FROM conversations")
    fun getAll(): List<ConversationsDto>

    @Insert
    fun insert(conversationsDto: ConversationsDto)

}