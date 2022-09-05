@file:OptIn(ExperimentalComposeUiApi::class)

package me.danlowe.meshcommunicator.ui.screen.signin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import me.danlowe.meshcommunicator.R
import me.danlowe.meshcommunicator.ui.button.StandardButton
import me.danlowe.meshcommunicator.ui.screen.signin.data.SignInEvent
import me.danlowe.meshcommunicator.ui.screen.signin.data.SignInNavEvent
import me.danlowe.meshcommunicator.ui.screen.signin.data.SignInState
import me.danlowe.meshcommunicator.ui.theme.Dimens
import me.danlowe.meshcommunicator.ui.theme.MeshCommunicatorTheme
import timber.log.Timber

@Composable
fun SignInScreen(
    viewModel: SignInViewModel = hiltViewModel(),
    navEventHandler: (SignInNavEvent) -> Unit
) {
    LaunchedEffect(key1 = Unit) {
        viewModel.events.collect { event ->
            when (event) {
                SignInEvent.Complete -> {
                    navEventHandler(SignInNavEvent.Complete)
                }
            }
        }
    }

    val isValidationError =
        when (viewModel.state.collectAsState(initial = SignInState.ValidName).value) {
            SignInState.InvalidName -> true
            SignInState.ValidName -> false
            SignInState.Error -> {
                // TODO show error screen when made
                return
            }
        }

    Timber.d("Start sign in: $viewModel, $navEventHandler")

    SignInContent(viewModel, isValidationError)
}

@Composable
private fun SignInContent(
    viewModel: SignInViewModel,
    isValidationError: Boolean
) {

    Timber.d("In content $viewModel, $isValidationError")

    val userNameValue = rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(""))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(PaddingValues(horizontal = 16.dp)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
    ) {
        UserNameField(userNameValue, viewModel, isValidationError)
        SignInButton(viewModel, userNameValue)
    }
}

@Composable
private fun UserNameField(
    userNameValue: MutableState<TextFieldValue>,
    viewModel: SignInViewModel,
    isValidationError: Boolean
) {

    val keyboardController = LocalSoftwareKeyboardController.current

    Column {
        OutlinedTextField(
            value = userNameValue.value,
            onValueChange = { newValue: TextFieldValue ->
                userNameValue.value = newValue
            },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(
                    Icons.Default.Face,
                    contentDescription = stringResource(R.string.username),
                )
            },
            singleLine = true,
            label = {
                Text(text = stringResource(R.string.username))
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                    viewModel.signIn(userNameValue.value.text)
                }
            ),
            isError = isValidationError
        )

        if (isValidationError) {
            Text(
                text = stringResource(R.string.username_error),
                color = MaterialTheme.colors.error,
                style = MaterialTheme.typography.caption,
                modifier = Modifier.padding(start = Dimens.BaseHorizontalSpace)
            )
        }
    }
}

@Composable
private fun SignInButton(
    viewModel: SignInViewModel,
    userNameValue: MutableState<TextFieldValue>
) {

    val keyboardController = LocalSoftwareKeyboardController.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
    ) {

        StandardButton(
            buttonText = R.string.btn_text_sign_in
        ) {
            keyboardController?.hide()
            viewModel.signIn(userNameValue.value.text)
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewSignInScreen() {
    val vm: SignInViewModel = viewModel()
    MeshCommunicatorTheme {
        SignInScreen(vm) {}
    }
}

