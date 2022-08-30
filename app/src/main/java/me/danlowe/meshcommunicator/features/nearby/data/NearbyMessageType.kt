@file:OptIn(ExperimentalSerializationApi::class)

package me.danlowe.meshcommunicator.features.nearby.data

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import me.danlowe.meshcommunicator.util.ext.toByteArray
import me.danlowe.meshcommunicator.util.ext.toHexString
import timber.log.Timber
import java.nio.ByteBuffer

/**
 * Message types for Nearby communication
 */
sealed class NearbyMessageType {

    /**
     * User is announcing their friendly name
     */
    @Serializable
    data class Name(
        val originUserId: String,
        val name: String,
    ) : NearbyMessageType()

    /**
     * A user readable message to be sent or received
     */
    @Serializable
    data class Message(
        val uuid: String,
        val conversationId: String,
        val originUserId: String,
        val message: String,
        val timeSent: Long,
        val timeReceived: Long,
    ) : NearbyMessageType()

    /**
     * An unknown message type. This should not be created directly and is used for tracking
     * malformed messages
     */
    class Unknown(val bytes: ByteArray) : NearbyMessageType()

    companion object {
        fun fromByteArray(bytes: ByteArray): NearbyMessageType {
            val buffer = ByteBuffer.wrap(bytes)
            val messageTypeCode = TypeCode.fromCode(buffer.int)

            Timber.d("Full array ${bytes.toHexString()}")

            val dataArray = bytes.copyOfRange(Int.SIZE_BYTES, bytes.lastIndex + 1)

            Timber.d("Data array: ${dataArray.toHexString()}")

            return when (messageTypeCode) {
                TypeCode.NAME -> {
                   ProtoBuf.decodeFromByteArray<Name>(dataArray)
                }
                TypeCode.MESSAGE -> {
                    ProtoBuf.decodeFromByteArray<Message>(dataArray)
                }
                TypeCode.UNKNOWN -> Unknown(bytes)
            }

        }

        fun toByteArray(type: NearbyMessageType): ByteArray {
            return when (type) {
                is Message -> {
                    TypeCode.MESSAGE.code.toByteArray() + ProtoBuf.encodeToByteArray(type)
                }
                is Name -> {
                    TypeCode.NAME.code.toByteArray() + ProtoBuf.encodeToByteArray(type)
                }
                is Unknown -> {
                    // Mostly just a stub, this type shouldn't be created externally
                    TypeCode.UNKNOWN.code.toByteArray()
                }
            }
        }

    }

    private enum class TypeCode(val code: Int) {
        NAME(1),
        MESSAGE(2),
        UNKNOWN(-1);

        companion object {

            fun fromCode(code: Int): TypeCode {
                return values().firstOrNull { typeCode ->
                    typeCode.code == code
                } ?: UNKNOWN
            }

        }
    }

}

fun NearbyMessageType.toByteArray(): ByteArray {
    return NearbyMessageType.toByteArray(this)
}
