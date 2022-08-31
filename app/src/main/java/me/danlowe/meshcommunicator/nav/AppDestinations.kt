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
        R.string.title_sign_in
    )

    object Conversations : AppDestinations(
        "Conversations",
        R.string.title_conversations
    )

    object Splash : AppDestinations(
        "Splash",
        R.string.title_splash
    )

    object NearbyPermissions : AppDestinations(
        "NearbyPermissions",
        R.string.title_nearby_permissions
    )

}