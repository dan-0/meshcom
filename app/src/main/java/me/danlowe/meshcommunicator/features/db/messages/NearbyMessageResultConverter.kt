package me.danlowe.meshcommunicator.features.db.messages

import androidx.room.TypeConverter
import me.danlowe.meshcommunicator.features.nearby.data.NearbyMessageResult

class NearbyMessageResultConverter {

    @TypeConverter
    fun fromNearbyMessageResult(result: NearbyMessageResult): Int {
        return result.id
    }

    @TypeConverter
    fun toNearbyMessageResult(code: Int): NearbyMessageResult {
        return NearbyMessageResult.idToResult(code)
    }

}
