package me.danlowe.meshcommunicator.di.db

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import me.danlowe.meshcommunicator.db.AppDatabase
import me.danlowe.meshcommunicator.db.conversations.ConversationsDao
import me.danlowe.meshcommunicator.db.messages.MessagesDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "main.db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideConversationsDao(appDatabase: AppDatabase): ConversationsDao {
        return appDatabase.conversationsDao()
    }

    @Provides
    @Singleton
    fun provideMessagesDao(appDatabase: AppDatabase): MessagesDao {
        return appDatabase.messagesDao()
    }

}