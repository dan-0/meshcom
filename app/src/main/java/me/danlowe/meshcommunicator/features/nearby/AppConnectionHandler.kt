package me.danlowe.meshcommunicator.features.nearby

import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import me.danlowe.meshcommunicator.features.appsettings.LiveAppSettings
import me.danlowe.meshcommunicator.features.dispatchers.DispatcherProvider
import me.danlowe.meshcommunicator.features.dispatchers.buildHandledIoContext
import me.danlowe.meshcommunicator.features.nearby.data.EndpointId
import me.danlowe.meshcommunicator.features.nearby.data.ExternalUserId
import me.danlowe.meshcommunicator.features.nearby.data.NearbyMessageResult
import me.danlowe.meshcommunicator.features.nearby.data.NearbyMessageType
import me.danlowe.meshcommunicator.features.nearby.data.toByteArray
import timber.log.Timber
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.random.Random

class AppConnectionHandler(
    private val dispatchers: DispatcherProvider,
    private val nearbyClient: ConnectionsClient,
    private val liveAppSettings: LiveAppSettings,
    private val activeConnections: ActiveConnections,
    private val messageHandler: MessageHandler
) {

    private val supervisorJob = SupervisorJob()

    private val scope = CoroutineScope(supervisorJob)

    private val dbContext = dispatchers.buildHandledIoContext { }

    private var localUserName: String = ""
    private var localUserId: String = ""

    val activeConnectionsState: Flow<Set<ExternalUserId>> = activeConnections.state

    private val payloadBuffer = MutableSharedFlow<ByteArray>(
        replay = 0,
        extraBufferCapacity = 100,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    init {
        scope.launch(dispatchers.io) {
            liveAppSettings.appSettings.collect {
                localUserName = it.userName
                localUserId = it.userId
                // TODO need to detect change and update connections accordingly
            }
        }

        scope.launch(dbContext) {
            payloadBuffer.collect { payloadBytes ->
                messageHandler.handlePayloadBytes(payloadBytes)
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
            val state = messageHandler.getAwaitingMessage(update.payloadId)

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

            handleNewConnection(endpoint, userId)
            Timber.d("connection initiated $endpoint")
        }

        override fun onConnectionResult(
            endpointId: String,
            connectionResolution: ConnectionResolution
        ) {
            Timber.d("Connection result: $endpointId ${connectionResolution.status}")

            when (connectionResolution.status.statusCode) {
                ConnectionsStatusCodes.SUCCESS -> {
                    sendNamePayload(endpointId)
                }
                ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
                    activeConnections.removeConnection(EndpointId(endpointId))
                }
            }
        }

        override fun onDisconnected(endpointId: String) {
            Timber.d("Connection disconnected")
            val endpoint = EndpointId(endpointId)
            activeConnections.removeConnection(endpoint)
        }
    }

    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {

        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            Timber.d("Endpoint found $endpointId")
            connectToEndpoint(EndpointId(endpointId), ExternalUserId(info.endpointName))
        }

        override fun onEndpointLost(endpointId: String) {
            Timber.d("Endpoint lost $endpointId")
            val endpoint = EndpointId(endpointId)
            activeConnections.removeConnection(endpoint)
        }
    }

    private fun connectToEndpoint(
        endpointId: EndpointId,
        externalUserId: ExternalUserId
    ) {

        scope.launch(dispatchers.io) {
            // This is a a brute force attempt that needs to be improved on
            withTimeout(ENDPOINT_CONNECT_TIMEOUT) {
                repeat(MAX_RECONNECT_ATTEMPTS) {
                    if (activeConnections.isConnectedToEndpoint(endpointId)) {
                        Timber.d("already connected")
                        return@withTimeout
                    }
                    val isConnected = suspendCancellableCoroutine { continuation ->
                        nearbyClient
                            .requestConnection(localUserId, endpointId.id, connectionLifecycleCallback)
                            .addOnSuccessListener {
                                Timber.d("Connection requested successfully: $endpointId")
                                handleNewConnection(endpointId, externalUserId)
                                continuation.resume(true)
                            }
                            .addOnFailureListener {
                                Timber.w(it, "Connection request failed $endpointId")
                                if (!activeConnections.isConnectedToEndpoint(endpointId)) {
                                    nearbyClient.disconnectFromEndpoint(endpointId.id)
                                }
                                continuation.resume(false)
                            }
                    }

                    if (isConnected) return@withTimeout
                    delay(Random.nextLong(RECONNECT_DELAY_LOW, RECONNECT_DELAY_HIGH))
                }
            }
        }
    }

    private fun handleNewConnection(
        endpointId: EndpointId,
        externalUserId: ExternalUserId
    ) {
        activeConnections.addConnection(
            endpointId = endpointId,
            externalUserId = externalUserId
        )
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

    private fun sendNamePayload(endpointId: String) {
        val namePayload = Payload.fromBytes(
            NearbyMessageType.Name(
                originUserId = localUserId,
                name = localUserName
            ).toByteArray()
        )
        nearbyClient.sendPayload(endpointId, namePayload)
    }

    suspend fun sendMessage(
        externalUserId: ExternalUserId,
        message: String,
        messageId: UUID
    ): Flow<NearbyMessageResult> {
        return messageHandler.sendMessage(externalUserId, message, messageId)
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

    companion object {

        private const val SERVICE_ID = "me.danlowe.meshcommunicator"

        private const val ENDPOINT_CONNECT_TIMEOUT = 10_000L

        private const val RECONNECT_DELAY_LOW = 500L

        private const val RECONNECT_DELAY_HIGH = 1000L

        private const val MAX_RECONNECT_ATTEMPTS = 3

    }

}
