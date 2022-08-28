package me.danlowe.meshcommunicator.db

import androidx.room.Database
import androidx.room.RoomDatabase
import me.danlowe.meshcommunicator.db.conversations.ConversationsDao
import me.danlowe.meshcommunicator.db.conversations.ConversationsDto
import me.danlowe.meshcommunicator.db.messages.MessageDto
import me.danlowe.meshcommunicator.db.messages.MessagesDao

@Database(
    entities = [
        ConversationsDto::class,
        MessageDto::class
    ],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun conversationsDao(): ConversationsDao
    abstract fun messagesDao(): MessagesDao
}