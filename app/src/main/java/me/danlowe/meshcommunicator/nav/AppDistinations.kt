package me.danlowe.meshcommunicator.nav

import androidx.annotation.StringRes
import me.danlowe.meshcommunicator.R

sealed class AppDestinations(
    private val baseRoute: String,
    @StringRes val title: Int
) {

    open val routeTemplate: String = baseRoute

    object SignIn : AppDestinations(
        "SignIn",
        R.string.sign_in
    )

}