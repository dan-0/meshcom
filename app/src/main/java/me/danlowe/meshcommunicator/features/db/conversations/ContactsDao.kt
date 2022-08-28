package me.danlowe.meshcommunicator.features.db.conversations

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

/**
 * DAO for conversations between different users
 */
@Dao
interface ContactsDao {

    @Query("SELECT * FROM contacts")
    suspend fun getAll(): List<ContactDto>

    @Query("SELECT * FROM contacts WHERE userId = :userId")
    suspend fun getByUserId(userId: String): ContactDto?

    @Update
    suspend fun updateContact(contact: ContactDto)

    @Insert
    suspend fun insert(conversationsDto: ContactDto)

}