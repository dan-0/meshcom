package me.danlowe.meshcommunicator.features.nearby

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import me.danlowe.meshcommunicator.features.nearby.data.EndpointId
import me.danlowe.meshcommunicator.features.nearby.data.ExternalUserId
import timber.log.Timber

class ActiveConnections {

    private val activeConnections = hashMapOf<ExternalUserId, EndpointId>()

    operator fun get(externalUserId: ExternalUserId): EndpointId? {
        return activeConnections[externalUserId]
    }

    private val _activeConnectionsState = MutableSharedFlow<Set<ExternalUserId>>(1, 0, BufferOverflow.DROP_OLDEST)
    val state: Flow<Set<ExternalUserId>> = _activeConnectionsState

    init {
        _activeConnectionsState.tryEmit(setOf())
    }

    fun addConnection(endpointId: EndpointId, externalUserId: ExternalUserId) {
        Timber.d("Adding connection $endpointId")
        activeConnections[externalUserId] = endpointId
        _activeConnectionsState.tryEmit(activeConnections.keys)
    }

    fun removeConnection(endpointId: EndpointId) {
        Timber.d("Removing connection $endpointId")
        val connection = activeConnections.firstNotNullOfOrNull { entry ->
            if (entry.value == endpointId) {
                entry.key
            } else {
                null
            }
        }
        activeConnections.remove(connection)
        _activeConnectionsState.tryEmit(activeConnections.keys)
    }

    fun clear() {
        activeConnections.clear()
        _activeConnectionsState.tryEmit(activeConnections.keys)
    }
}