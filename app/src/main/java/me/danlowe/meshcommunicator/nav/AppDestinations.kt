package me.danlowe.meshcommunicator.nav

import androidx.annotation.StringRes
import androidx.navigation.NavBackStackEntry
import me.danlowe.meshcommunicator.R
import me.danlowe.meshcommunicator.features.nearby.data.ExternalUserId

sealed class AppDestinations(
    val baseRoute: String,
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

    object Conversation : AppDestinations(
        "Conversation",
        R.string.title_conversation
    ) {

        const val PARAM_EXTERNAL_USER_ID = "PARAM_EXTERNAL_USER_ID"

        private const val PARAM_EXTERNAL_USER_NAME = "PARAM_EXTERNAL_USER_NAME"

        override val routeTemplate: String = "$baseRoute/{$PARAM_EXTERNAL_USER_ID}/{$PARAM_EXTERNAL_USER_NAME}"

        fun buildRoute(externalUserId: ExternalUserId, userName: String): String {
            return "$baseRoute/${externalUserId.id}/$userName"
        }

        fun externalIdFromBackstack(backStackEntry: NavBackStackEntry): ExternalUserId {
            return ExternalUserId(
                backStackEntry.arguments!!.getString(PARAM_EXTERNAL_USER_ID)!!
            )
        }

        fun userNameFromBackstack(backStackEntry: NavBackStackEntry): String {
            return backStackEntry.arguments!!.getString(PARAM_EXTERNAL_USER_NAME)!!
        }

    }

}