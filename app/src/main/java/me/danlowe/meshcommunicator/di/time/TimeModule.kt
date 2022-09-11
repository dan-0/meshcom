package me.danlowe.meshcommunicator.di.time

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import me.danlowe.meshcommunicator.util.helper.time.TimeFormatter
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class TimeModule {

    @Provides
    @Singleton
    fun provideTimeFormatter(
        @ApplicationContext context: Context
    ): TimeFormatter {
        return TimeFormatter(context)
    }

}
