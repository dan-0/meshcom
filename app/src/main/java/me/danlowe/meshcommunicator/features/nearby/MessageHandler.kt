package me.danlowe.meshcommunicator.features.nearby

import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.Payload
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import me.danlowe.meshcommunicator.features.appsettings.LiveAppSettings
import me.danlowe.meshcommunicator.features.db.conversations.ContactDto
import me.danlowe.meshcommunicator.features.db.conversations.ContactsDao
import me.danlowe.meshcommunicator.features.db.messages.MessageDto
import me.danlowe.meshcommunicator.features.db.messages.MessagesDao
import me.danlowe.meshcommunicator.features.dispatchers.DispatcherProvider
import me.danlowe.meshcommunicator.features.dispatchers.buildHandledIoContext
import me.danlowe.meshcommunicator.features.nearby.data.EndpointId
import me.danlowe.meshcommunicator.features.nearby.data.ExternalUserId
import me.danlowe.meshcommunicator.features.nearby.data.NearbyMessageResult
import me.danlowe.meshcommunicator.features.nearby.data.NearbyMessageType
import me.danlowe.meshcommunicator.features.nearby.data.toByteArray
import me.danlowe.meshcommunicator.util.ext.toHexString
import timber.log.Timber
import java.time.Instant
import java.util.UUID

class MessageHandler(
    dispatchers: DispatcherProvider,
    private val contactsDao: ContactsDao,
    private val messagesDao: MessagesDao,
    private val liveAppSettings: LiveAppSettings,
    private val activeConnections: ActiveConnections,
    private val nearbyClient: ConnectionsClient,
) {

    private val databaseContext = dispatchers.buildHandledIoContext {  }

    private val handlerJob = SupervisorJob()

    private val scope = CoroutineScope(handlerJob + databaseContext)

    private val awaitingMessages = mutableMapOf<Long, MutableStateFlow<AwaitingMessageState>>()

    fun getAwaitingMessage(payloadId: Long): MutableStateFlow<AwaitingMessageState>? {
        return awaitingMessages[payloadId]
    }

    suspend fun handlePayloadBytes(payloadBytes: ByteArray) {

        when (val type = NearbyMessageType.fromByteArray(payloadBytes)) {
            is NearbyMessageType.Message -> {
                Timber.d("New message")
                handleMessage(type)
            }
            is NearbyMessageType.Name -> {
                Timber.d("New name")
                handleName(type)
            }
            is NearbyMessageType.Unknown -> {
                /*
                    Note, this is for debugging in a hobby project. Please don't log
                    potentially personal user data if your users think this data is
                    secure.
                 */
                Timber.e("Unknown message type with data: ${type.bytes.toHexString()}")
            }
        }
    }

    private suspend fun handleName(type: NearbyMessageType.Name) {
        val contact = contactsDao.getByUserId(type.originUserId)

        val lastSeen = Instant.now().toEpochMilli()

        if (contact == null) {
            val dto = ContactDto(
                userName = type.name,
                externalUserId = type.originUserId,
                lastSeen = lastSeen
            )
            contactsDao.insert(dto)
        } else {
            contactsDao.updateContact(
                contact.copy(
                    userName = type.name,
                    lastSeen = lastSeen
                )
            )
        }

        /*
         * Sending here as a "name" is sent by the other client whenever we successfully establish
         * a connection. A name means we're past the initialization of the connection and have
         * up to date information on the other client.
         */
        sendUnsentMessages(ExternalUserId(type.originUserId))
    }

    private suspend fun handleMessage(type: NearbyMessageType.Message) {
        val dto = MessageDto(
            uuid = type.uuid,
            originUserId = type.originUserId,
            targetUserId = liveAppSettings.appSettings.value.userId,
            message = type.message,
            timeSent = type.timeSent,
            timeReceived = Instant.now().toEpochMilli(),
            sendState = NearbyMessageResult.None
        )

        messagesDao.insert(dto)

        contactsDao.getByUserId(type.originUserId)?.copy(
            lastSeen = Instant.now().toEpochMilli()
        )?.also { contact ->
            contactsDao.updateContact(contact)
        }
    }

    private fun sendUnsentMessages(
        externalUserId: ExternalUserId
    ) {
        val endpointId = activeConnections.getEndpoint(externalUserId)

        endpointId ?: run {
            Timber.w("Tried to send unsent messages to unknown endpoint for $externalUserId")
            return
        }

        scope.launch {
            val messages = messagesDao.getUnsentMessagesFromUser(externalUserId.id)
            Timber.d("Unread messages $messages")
            messages.forEach { dto ->
                launch {
                    val nearbyMessage = NearbyMessageType.Message(
                        uuid = dto.uuid,
                        originUserId = dto.originUserId,
                        message = dto.message,
                        timeSent = dto.timeSent,
                    ).toByteArray()

                    val payload = Payload.fromBytes(nearbyMessage)

                    val resultingDto = when (sendPayloadForResult(payload, endpointId)) {
                        AwaitingMessageState.Success -> {
                            dto.copy(
                                sendState = NearbyMessageResult.Success,
                                timeReceived = Instant.now().toEpochMilli()
                            )
                        }
                        AwaitingMessageState.Fail,
                        AwaitingMessageState.Created,
                        null -> {
                            dto.copy(
                                sendState = NearbyMessageResult.Error
                            )
                        }
                    }

                    messagesDao.update(resultingDto)
                }
            }
        }
    }

    suspend fun sendPayloadForResult(
        payload: Payload,
        endpointId: EndpointId
    ): AwaitingMessageState? {
        val awaitingState = MutableStateFlow<AwaitingMessageState>(
            AwaitingMessageState.Created
        )
        awaitingMessages[payload.id] = awaitingState

        nearbyClient.sendPayload(endpointId.id, payload)

        val result = withTimeoutOrNull(MESSAGE_STATUS_WAIT_TIME) {
            awaitingState.first {
                it != AwaitingMessageState.Created
            }
        }

        awaitingMessages.remove(payload.id)

        return result
    }

    suspend fun sendMessage(
        externalUserId: ExternalUserId,
        message: String,
        messageId: UUID
    ): Flow<NearbyMessageResult> {

        val endpointId = activeConnections[externalUserId]

        val sendState = if (endpointId == null) {
            NearbyMessageResult.NoEndpoint
        } else {
            NearbyMessageResult.Sending
        }

        val dto = MessageDto(
            uuid = messageId.toString(),
            originUserId = liveAppSettings.appSettings.value.userId,
            targetUserId = externalUserId.id,
            message = message,
            timeSent = Instant.now().toEpochMilli(),
            timeReceived = MessageDto.NO_RECEIVED_TIME,
            sendState = sendState
        )

        messagesDao.insert(dto)

        val resolvedMessage = messagesDao.getByUuid(messageId.toString())

        return buildSendMessageFlow(sendState, dto, endpointId).onEach { messageResult ->
            resolvedMessage?.let { resolvedDto ->
                val updatedDto = when (messageResult) {
                    NearbyMessageResult.Error,
                    NearbyMessageResult.NoEndpoint,
                    NearbyMessageResult.None,
                    NearbyMessageResult.Sending -> resolvedDto.copy(
                        sendState = messageResult
                    )
                    NearbyMessageResult.Success -> resolvedDto.copy(
                        sendState = messageResult,
                        timeReceived = Instant.now().toEpochMilli()
                    )
                }
                messagesDao.update(updatedDto)
            }
        }

    }

    private fun buildSendMessageFlow(
        sendState: NearbyMessageResult,
        dto: MessageDto,
        endpointId: EndpointId?
    ) = flow {
        if (sendState is NearbyMessageResult.NoEndpoint) {
            Timber.d("Attempted to send message to disconnected endpoint")
            emit(sendState)
            return@flow
        }

        emit(NearbyMessageResult.Sending)

        val nearbyMessage = NearbyMessageType.Message(
            uuid = dto.uuid,
            originUserId = dto.originUserId,
            message = dto.message,
            timeSent = dto.timeSent
        ).toByteArray()

        val payload = Payload.fromBytes(nearbyMessage)

        val result = sendPayloadForResult(payload, endpointId!!)

        Timber.d("Result: $result")

        when (result) {
            AwaitingMessageState.Success -> {
                emit(NearbyMessageResult.Success)
            }
            AwaitingMessageState.Fail,
            AwaitingMessageState.Created,
            null -> {
                emit(NearbyMessageResult.Error)
            }
        }

    }

    companion object {

        private const val MESSAGE_STATUS_WAIT_TIME = 5_000L

    }

}
