package me.danlowe.meshcommunicator.di.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import me.danlowe.meshcommunicator.AppSettings
import me.danlowe.meshcommunicator.features.datastore.AppSettingsSerializer
import javax.inject.Singleton

/**
 * There's no reason to use DataStore here, it's half baked and much more complicated than the use
 * case requires, but this is a hobby app....
 */
@Module
@InstallIn(SingletonComponent::class)
class DataStoreModule {

    private val Context.settingsStore: DataStore<AppSettings> by dataStore(
            "appsettings.pb",
        AppSettingsSerializer
    )

    @Provides
    @Singleton
    fun providesSettingsDatastore(
        @ApplicationContext context: Context
    ): DataStore<AppSettings> {
        return context.settingsStore
    }

}

