package me.danlowe.meshcommunicator.features.nearby

import androidx.datastore.core.DataStore
import com.google.android.gms.nearby.connection.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import me.danlowe.meshcommunicator.AppSettings
import me.danlowe.meshcommunicator.features.db.conversations.ContactDto
import me.danlowe.meshcommunicator.features.db.conversations.ContactsDao
import me.danlowe.meshcommunicator.features.db.messages.MessageDto
import me.danlowe.meshcommunicator.features.db.messages.MessagesDao
import me.danlowe.meshcommunicator.features.dispatchers.DispatcherProvider
import me.danlowe.meshcommunicator.features.dispatchers.buildHandledIoContext
import me.danlowe.meshcommunicator.features.nearby.data.*
import me.danlowe.meshcommunicator.util.ext.toHexString
import timber.log.Timber
import java.time.Instant
import java.util.*

class AppConnections(
    dispatchers: DispatcherProvider,
    private val nearbyClient: ConnectionsClient,
    private val appSettings: DataStore<AppSettings>,
    private val contactsDao: ContactsDao,
    private val messagesDao: MessagesDao,
    private val activeConnections: ActiveConnections
) {

    private val supervisorJob = SupervisorJob()

    private val scope = CoroutineScope(supervisorJob)

    private val dbContext = dispatchers.buildHandledIoContext { }

    private var localUserName: String = ""
    private var localUserId: String = ""

    val activeConnectionsState: StateFlow<Set<ExternalUserId>>
        get() = activeConnections.state

    private val awaitingMessages = mutableMapOf<Long, MutableStateFlow<AwaitingMessageState>>()

    private val payloadBuffer = MutableSharedFlow<ByteArray>(0, 100, BufferOverflow.DROP_OLDEST)

    init {
        scope.launch(dispatchers.io) {
            appSettings.data.collect {
                localUserName = it.userName
                localUserId = it.userId
                // TODO need to detect change and update connections accordingly
            }
        }

        scope.launch(dbContext) {
            payloadBuffer.collect { payloadBytes ->
                val type = NearbyMessageType.fromByteArray(payloadBytes)

                when (type) {
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
        }
    }

    private val advertisingOptions = AdvertisingOptions.Builder()
        .setStrategy(Strategy.P2P_CLUSTER)
        .build()

    private val discoveryOptions = DiscoveryOptions.Builder()
        .setStrategy(Strategy.P2P_CLUSTER)
        .build()

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            when (payload.type) {
                Payload.Type.BYTES -> {
                    val bytes = payload.asBytes() ?: return
                    payloadBuffer.tryEmit(bytes)
                }
            }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
            val state = awaitingMessages[update.payloadId]

            state?.value = when (update.status) {
                PayloadTransferUpdate.Status.SUCCESS -> {
                    AwaitingMessageState.Success
                }
                PayloadTransferUpdate.Status.IN_PROGRESS -> {
                    AwaitingMessageState.Created
                }
                PayloadTransferUpdate.Status.CANCELED,
                PayloadTransferUpdate.Status.FAILURE -> {
                    AwaitingMessageState.Fail
                }
                else -> throw IllegalArgumentException("Received unknown payload status")
            }
        }

    }

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {

        override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
            // TODO Need to add some kind of authentication procedure
            nearbyClient.acceptConnection(endpointId, payloadCallback)

            val endpoint = EndpointId(endpointId)
            val userId = ExternalUserId(connectionInfo.endpointName)

            activeConnections.addConnection(endpoint, userId)
        }

        override fun onConnectionResult(
            endpointId: String,
            connectionResolution: ConnectionResolution
        ) {
            Timber.d("Connection result: $endpointId ${connectionResolution.status}")

            when (connectionResolution.status.statusCode) {
                ConnectionsStatusCodes.SUCCESS -> {

                    val namePayload = Payload.fromBytes(
                        NearbyMessageType.Name(
                            originUserId = localUserId,
                            name = localUserName
                        ).toByteArray()
                    )
                    nearbyClient.sendPayload(endpointId, namePayload)
                }
                ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
                    activeConnections.removeConnection(EndpointId(endpointId))
                }
            }
        }

        override fun onDisconnected(endpointId: String) {
            val endpoint = EndpointId(endpointId)
            activeConnections.removeConnection(endpoint)
        }
    }

    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {

        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            nearbyClient
                .requestConnection(localUserId, endpointId, connectionLifecycleCallback)
                .addOnSuccessListener {
                    Timber.d("Connection requested successfully")
                    activeConnections.addConnection(
                        endpointId = EndpointId(endpointId),
                        externalUserId = ExternalUserId(info.endpointName)
                    )
                }
                .addOnFailureListener {
                    // TODO
                    Timber.w(it, "Connection request failed")
                }
        }

        override fun onEndpointLost(endpointId: String) {
            val endpoint = EndpointId(endpointId)
            activeConnections.removeConnection(endpoint)
        }
    }

    fun start() {
        stop()
        startAdvertising()
        startDiscovery()
    }

    fun stop() {
        nearbyClient.stopAdvertising()
        nearbyClient.stopDiscovery()
        nearbyClient.stopAllEndpoints()
        activeConnections.clear()
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
            originUserId = localUserId,
            message = message,
            timeSent = Instant.now().toEpochMilli(),
            timeReceived = -1,
            sendState = sendState
        )

        messagesDao.insert(dto)

        val resolvedMessage = messagesDao.getByUuid(messageId.toString())

        return flow {
            if (sendState is NearbyMessageResult.NoEndpoint) {
                Timber.d("Attempted to send message to disconnected endpoint")
                emit(sendState)
                return@flow
            }

            val nearbyMessage = NearbyMessageType.Message(
                uuid = dto.uuid,
                originUserId = dto.originUserId,
                message = dto.message,
                timeSent = dto.timeSent,
                timeReceived = -1
            ).toByteArray()

            val payload = Payload.fromBytes(nearbyMessage)

            val awaitingState = MutableStateFlow<AwaitingMessageState>(AwaitingMessageState.Created)
            awaitingMessages[payload.id] = awaitingState

            /*
                endpointId should not be null, this is checked prior, resulting in a returned
                NoEndpoint state
             */
            nearbyClient.sendPayload(endpointId!!.id, payload)

            emit(NearbyMessageResult.Sending)

            val result = withTimeoutOrNull(MESSAGE_STATUS_WAIT_TIME) {

                awaitingState.first {
                    it != AwaitingMessageState.Created
                }

            }

            Timber.d("Result: $result")

            when (result) {
                AwaitingMessageState.Success -> emit(NearbyMessageResult.Success)
                AwaitingMessageState.Fail,
                AwaitingMessageState.Created,
                null -> {
                    emit(NearbyMessageResult.Error)
                }
            }

            awaitingMessages.remove(payload.id)
        }.onEach { messageResult ->
            resolvedMessage?.let { resolvedDto ->
                resolvedDto.copy(sendState = messageResult).also {
                    messagesDao.update(it)
                }
            }
        }

    }

    private fun startAdvertising() {

        nearbyClient.startAdvertising(
            localUserId, SERVICE_ID, connectionLifecycleCallback, advertisingOptions
        )
            .addOnSuccessListener {
                Timber.d("Advertising started")
            }
            .addOnFailureListener {
                Timber.w(it, "Advertising failed")
            }
    }

    private fun startDiscovery() {
        nearbyClient.startDiscovery(SERVICE_ID, endpointDiscoveryCallback, discoveryOptions)
            .addOnSuccessListener {
                // TODO
                Timber.d("Discovery started")
            }
            .addOnFailureListener {
                // TODO
                Timber.w(it, "Discovery failed")
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
    }

    private suspend fun handleMessage(type: NearbyMessageType.Message) {
        val dto = MessageDto(
            uuid = type.uuid,
            originUserId = type.originUserId,
            message = type.message,
            timeSent = type.timeSent,
            timeReceived = type.timeReceived,
            sendState = NearbyMessageResult.None
        )

        messagesDao.insert(dto)

        contactsDao.getByUserId(type.originUserId)?.copy(
            lastSeen = Instant.now().toEpochMilli()
        )?.also { contact ->
            contactsDao.updateContact(contact)
        }
    }

    companion object {

        private const val SERVICE_ID = "me.danlowe.meshcommunicator"

        private const val MESSAGE_STATUS_WAIT_TIME = 5_000L

    }

    private sealed class AwaitingMessageState {

        object Created : AwaitingMessageState()

        object Success : AwaitingMessageState()

        object Fail : AwaitingMessageState()

    }

}