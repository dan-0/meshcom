package me.danlowe.meshcommunicator.test.helper

import androidx.annotation.StringRes
import androidx.test.platform.app.InstrumentationRegistry

fun getStringById(@StringRes id: Int): String {
    return InstrumentationRegistry.getInstrumentation().targetContext.getString(id)
}