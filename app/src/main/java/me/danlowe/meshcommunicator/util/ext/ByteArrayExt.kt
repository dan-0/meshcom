package me.danlowe.meshcommunicator.util.ext

fun ByteArray.toHexString(): String {
    return joinToString("") { "%02x".format(it) }
}