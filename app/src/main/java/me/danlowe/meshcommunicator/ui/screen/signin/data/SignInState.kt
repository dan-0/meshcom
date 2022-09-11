package me.danlowe.meshcommunicator.ui.screen.signin.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class SignInState : Parcelable {

    @Parcelize
    object ValidName : SignInState()

    @Parcelize
    object InvalidName : SignInState()

    @Parcelize
    object Error : SignInState()

}
