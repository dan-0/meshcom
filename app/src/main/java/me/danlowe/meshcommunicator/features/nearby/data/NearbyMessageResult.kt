package me.danlowe.meshcommunicator.features.nearby.data

sealed class NearbyMessageResult(
    val id: Int
) {

    object None : NearbyMessageResult(-1)

    object Error : NearbyMessageResult(0)

    object NoEndpoint : NearbyMessageResult(1)

    object Sending : NearbyMessageResult(2)

    object Success : NearbyMessageResult(3)

    companion object {

        fun idToResult(id: Int): NearbyMessageResult {
            return when (id) {
                0 -> Error
                1 -> NoEndpoint
                2 -> Sending
                3 -> Success
                else -> None
            }
        }

    }

}