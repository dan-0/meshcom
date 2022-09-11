package me.danlowe.meshcommunicator.di.dispatchers

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.danlowe.meshcommunicator.features.dispatchers.DispatcherProvider
import me.danlowe.meshcommunicator.features.dispatchers.DispatcherProviderImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DispatchersModule {

    @Provides
    @Singleton
    fun providesDispatchers(): DispatcherProvider {
        return DispatcherProviderImpl()
    }

}
