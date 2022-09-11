package me.danlowe.meshcommunicator.features.db

import androidx.room.Database
import androidx.room.RoomDatabase
import me.danlowe.meshcommunicator.features.db.conversations.ContactDto
import me.danlowe.meshcommunicator.features.db.conversations.ContactsDao
import me.danlowe.meshcommunicator.features.db.messages.MessageDto
import me.danlowe.meshcommunicator.features.db.messages.MessagesDao

@Database(
    entities = [
        ContactDto::class,
        MessageDto::class
    ],
    version = 7
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun contactsDao(): ContactsDao
    abstract fun messagesDao(): MessagesDao
}