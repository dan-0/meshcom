package me.danlowe.meshcommunicator.features.nearby

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import me.danlowe.meshcommunicator.features.nearby.data.EndpointId
import me.danlowe.meshcommunicator.features.nearby.data.ExternalUserId
import timber.log.Timber

class ActiveConnections {

    private val _activeConnections = hashMapOf<ExternalUserId, EndpointId>()

    operator fun get(externalUserId: ExternalUserId): EndpointId? {
        return _activeConnections[externalUserId]
    }

    private val _activeConnectionsState = MutableStateFlow(_activeConnections.keys)
    val state: StateFlow<Set<ExternalUserId>> = _activeConnectionsState

    fun addConnection(endpointId: EndpointId, externalUserId: ExternalUserId) {
        Timber.d("Adding connection $endpointId")
        _activeConnections[externalUserId] = endpointId
        _activeConnectionsState.value = _activeConnections.keys
    }

    fun removeConnection(endpointId: EndpointId) {
        Timber.d("Removing connection $endpointId")
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

    fun clear() {
        _activeConnections.clear()
        _activeConnectionsState.value = _activeConnections.keys
    }
}