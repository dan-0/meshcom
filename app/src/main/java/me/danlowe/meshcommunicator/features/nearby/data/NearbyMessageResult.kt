package me.danlowe.meshcommunicator.features.nearby.data

sealed class NearbyMessageResult(
    val id: Int
) {

    object None : NearbyMessageResult(id = -1)

    object Error : NearbyMessageResult(id = 0)

    object NoEndpoint : NearbyMessageResult(id = 1)

    object Sending : NearbyMessageResult(id = 2)

    object Success : NearbyMessageResult(id = 3)

    companion object {

        fun idToResult(id: Int): NearbyMessageResult {
            return when (id) {
                None.id -> None
                Error.id -> Error
                NoEndpoint.id -> NoEndpoint
                Sending.id -> Sending
                Success.id -> Success
                else -> throw IllegalArgumentException("Unknown ID $id")
            }
        }

    }

}
