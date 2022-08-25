package me.danlowe.meshcommunicator.features.datastore

import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import me.danlowe.meshcommunicator.AppSettings
import timber.log.Timber
import java.io.InputStream
import java.io.OutputStream

@Suppress("BlockingMethodInNonBlockingContext")
object AppSettingsSerializer : Serializer<AppSettings> {
    override val defaultValue: AppSettings = AppSettings.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): AppSettings {
        try {
            return AppSettings.parseFrom(input)
        } catch (e: InvalidProtocolBufferException) {
            Timber.e(e)
            throw e
        }
    }

    override suspend fun writeTo(t: AppSettings, output: OutputStream) {
        t.writeTo(output)
    }

}