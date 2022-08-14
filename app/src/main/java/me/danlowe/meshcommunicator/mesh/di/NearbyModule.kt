package me.danlowe.meshcommunicator.mesh.di

import android.content.Context
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.ConnectionsClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class NearbyModule {

    @Provides
    @Singleton
    fun provideNearbyModule(
        @ApplicationContext context: Context
    ): ConnectionsClient {
        return Nearby.getConnectionsClient(context)
    }

}