package me.danlowe.meshcommunicator.di.dispatchers

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.danlowe.meshcommunicator.features.dispatchers.DispatcherProvider
import me.danlowe.meshcommunicator.features.dispatchers.DispatcherProviderImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class DispatchersModule {

    @Binds
    abstract fun bindDispatchers(dispatcherProvider: DispatcherProviderImpl): DispatcherProvider

}