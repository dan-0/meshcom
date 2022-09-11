package me.danlowe.meshcommunicator.di.nearby

import android.content.Context
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.ConnectionsClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import me.danlowe.meshcommunicator.features.appsettings.LiveAppSettings
import me.danlowe.meshcommunicator.features.db.AppDatabase
import me.danlowe.meshcommunicator.features.dispatchers.DispatcherProvider
import me.danlowe.meshcommunicator.features.nearby.ActiveConnections
import me.danlowe.meshcommunicator.features.nearby.AppConnectionHandler
import me.danlowe.meshcommunicator.features.nearby.MessageHandler
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class NearbyModule {

    @Provides
    @Singleton
    fun provideNearbyClient(
        @ApplicationContext context: Context
    ): ConnectionsClient {
        return Nearby.getConnectionsClient(context)
    }

    @Provides
    @Singleton
    fun provideNearbyConnections(
        dispatchers: DispatcherProvider,
        nearbyClient: ConnectionsClient,
        activeConnections: ActiveConnections,
        liveAppSettings: LiveAppSettings,
        incomingMessageHandler: MessageHandler
    ): AppConnectionHandler {
        return AppConnectionHandler(
            dispatchers = dispatchers,
            nearbyClient = nearbyClient,
            activeConnections = activeConnections,
            liveAppSettings = liveAppSettings,
            messageHandler = incomingMessageHandler,
        )
    }

    @Provides
    @Singleton
    fun provideActiveConnections(): ActiveConnections {
        return ActiveConnections()
    }

    @Provides
    @Singleton
    fun provideIncomingMessageHandler(
        dispatchers: DispatcherProvider,
        appDatabase: AppDatabase,
        liveAppSettings: LiveAppSettings,
        nearbyClient: ConnectionsClient,
        activeConnections: ActiveConnections,
    ): MessageHandler {
        return MessageHandler(
            dispatchers = dispatchers,
            contactsDao = appDatabase.contactsDao(),
            messagesDao = appDatabase.messagesDao(),
            liveAppSettings = liveAppSettings,
            nearbyClient = nearbyClient,
            activeConnections = activeConnections
        )
    }

}
