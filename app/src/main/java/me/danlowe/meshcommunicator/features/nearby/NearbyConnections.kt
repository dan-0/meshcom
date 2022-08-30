package me.danlowe.meshcommunicator.features.nearby

import androidx.datastore.core.DataStore
import com.google.android.gms.nearby.connection.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import me.danlowe.meshcommunicator.AppSettings
import me.danlowe.meshcommunicator.features.db.conversations.ContactDto
import me.danlowe.meshcommunicator.features.db.conversations.ContactsDao
import me.danlowe.meshcommunicator.features.db.messages.MessageDto
import me.danlowe.meshcommunicator.features.db.messages.MessagesDao
import me.danlowe.meshcommunicator.features.dispatchers.DispatcherProvider
import me.danlowe.meshcommunicator.features.dispatchers.buildHandledIoContext
import me.danlowe.meshcommunicator.features.nearby.data.EndpointId
import me.danlowe.meshcommunicator.features.nearby.data.ExternalUserId
import me.danlowe.meshcommunicator.features.nearby.data.NearbyMessageType
import me.danlowe.meshcommunicator.features.nearby.data.toByteArray
import me.danlowe.meshcommunicator.util.ext.toHexString
import timber.log.Timber
import java.time.Instant

class NearbyConnections(
    dispatchers: DispatcherProvider,
    private val nearbyClient: ConnectionsClient,
    private val appSettings: DataStore<AppSettings>,
    private val contactsDao: ContactsDao,
    private val messagesDao: MessagesDao,
) {

    private val supervisorJob = SupervisorJob()

    private val scope = CoroutineScope(supervisorJob)

    private val dbContext = dispatchers.buildHandledIoContext { }

    private var localUserName: String = ""
    private var localUserId: String = ""

    private val _activeConnections = hashMapOf<ExternalUserId, EndpointId>()

    private val _activeConnectionsState = MutableStateFlow(_activeConnections.keys)
    val activeConnectionsState: Flow<Set<ExternalUserId>> = _activeConnectionsState

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
                        handleMessage(type)
                    }
                    is NearbyMessageType.Name -> {
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
            // Nothing to do here
        }

    }

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {

        override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
            // TODO Need to add some kind of authentication procedure
            nearbyClient.acceptConnection(endpointId, payloadCallback)

            val endpoint = EndpointId(endpointId)
            val userId = ExternalUserId(connectionInfo.endpointName)
            addConnection(endpoint, userId)
        }

        override fun onConnectionResult(
            endpointId: String,
            connectionResolution: ConnectionResolution
        ) {
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
            }
        }

        override fun onDisconnected(endpointId: String) {
            val endpoint = EndpointId(endpointId)
            removeConnection(endpoint)
        }
    }

    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {

        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            nearbyClient
                .requestConnection(localUserName, endpointId, connectionLifecycleCallback)
                .addOnSuccessListener {
                    // TODO
                    Timber.d("Connection requested successfully")
                }
                .addOnFailureListener {
                    // TODO
                    Timber.w(it, "Connection request failed")
                }
        }

        override fun onEndpointLost(endpointId: String) {
            val endpoint = EndpointId(endpointId)
            removeConnection(endpoint)
        }
    }

    fun start()  {
        startAdvertising()
        startDiscovery()
    }

    fun stop() {
        stopAdvertising()
        stopDiscovery()
        nearbyClient.stopAllEndpoints()
    }


    private fun startAdvertising() {

        nearbyClient.startAdvertising(
            localUserName, SERVICE_ID, connectionLifecycleCallback, advertisingOptions
        )
            .addOnSuccessListener {
                Timber.d("Advertising started")
            }
            .addOnFailureListener {
                Timber.w(it, "Advertising failed")
            }
    }

    private fun stopAdvertising() {
        nearbyClient.stopAdvertising()
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

    private fun stopDiscovery() {
        nearbyClient.stopDiscovery()
    }

    private fun addConnection(endpointId: EndpointId, externalUserId: ExternalUserId) {
        _activeConnections[externalUserId] = endpointId
        _activeConnectionsState.value = _activeConnections.keys
    }

    private fun removeConnection(endpointId: EndpointId) {
        val connection = _activeConnections.firstNotNullOfOrNull { entry ->
            if (entry.value == endpointId) {
                entry.key
            } else {
                null
            }
        }
        _activeConnections.remove(connection)
        _activeConnectionsState.value = _activeConnections.keys
    }

    private suspend fun handleName(type: NearbyMessageType.Name) {
        val contact = contactsDao.getByUserId(type.originUserId)

        val lastSeen = Instant.now().toEpochMilli()

        if (contact == null) {
            val dto = ContactDto(
                userName = type.name,
                userId = type.originUserId,
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
            conversationId = type.conversationId,
            originUserId = type.originUserId,
            message = type.message,
            timeSent = type.timeSent,
            timeReceived = type.timeReceived
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

    }

}

