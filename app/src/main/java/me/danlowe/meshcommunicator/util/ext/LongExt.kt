package me.danlowe.meshcommunicator.util.ext

import java.time.Instant

fun Long.asMilliToInstant(): Instant = Instant.ofEpochMilli(this)

fun Long.toIso8601String(): String = asMilliToInstant().toString()
