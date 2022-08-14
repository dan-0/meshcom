package me.danlowe.meshcommunicator.mesh.viewmodel

import androidx.lifecycle.ViewModel
import com.google.android.gms.nearby.connection.ConnectionsClient
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MeshViewModel @Inject constructor(
    private val nearbyClient: ConnectionsClient
) : ViewModel() {


}

