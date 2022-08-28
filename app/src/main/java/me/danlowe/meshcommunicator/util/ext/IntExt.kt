package me.danlowe.meshcommunicator.util.ext

import java.nio.ByteBuffer

fun Int.toByteArray(): ByteArray {
    return ByteBuffer.allocate(Int.SIZE_BYTES).putInt(this).array()
}
