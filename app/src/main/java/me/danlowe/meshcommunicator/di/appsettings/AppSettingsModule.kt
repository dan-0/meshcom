package me.danlowe.meshcommunicator.di.appsettings

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.danlowe.meshcommunicator.features.appsettings.LiveAppSettings
import me.danlowe.meshcommunicator.features.appsettings.LiveAppSettingsImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppSettingsModule {

    @Binds
    @Singleton
    abstract fun bindsAppSettings(impl: LiveAppSettingsImpl): LiveAppSettings

}
