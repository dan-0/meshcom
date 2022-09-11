package me.danlowe.meshcommunicator.features.nearby.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * The unique identifier set by an external user for connection
 */
@JvmInline
@Parcelize
value class ExternalUserId(val id: String) : Parcelable
