package me.danlowe.meshcommunicator.di.nearby

import android.content.Context
import androidx.datastore.core.DataStore
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.ConnectionsClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import me.danlowe.meshcommunicator.AppSettings
import me.danlowe.meshcommunicator.features.db.AppDatabase
import me.danlowe.meshcommunicator.features.dispatchers.DispatcherProvider
import me.danlowe.meshcommunicator.features.nearby.NearbyConnections
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
        appSettings: DataStore<AppSettings>,
        appDatabase: AppDatabase
    ): NearbyConnections {
        return NearbyConnections(
            dispatchers,
            nearbyClient,
            appSettings,
            appDatabase.contactsDao(),
            appDatabase.messagesDao()
        )
    }

}