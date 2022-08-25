package me.danlowe.meshcommunicator.ui.screen.signin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import me.danlowe.meshcommunicator.R
import me.danlowe.meshcommunicator.ui.screen.signin.data.SignInState
import me.danlowe.meshcommunicator.ui.theme.Dimens
import me.danlowe.meshcommunicator.ui.theme.MeshCommunicatorTheme

@Composable
fun SignInScreen(viewModel: SignInViewModel) {
    val userNameValue = rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(""))
    }

    val isError = when (viewModel.state.collectAsState(initial = SignInState.ValidName).value) {
        SignInState.InvalidName -> true
        SignInState.ValidName -> false
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(PaddingValues(horizontal = 16.dp)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
    ) {
        UserNameField(userNameValue, isError)
        SignInButton(viewModel, userNameValue)
    }
}

@Composable
private fun UserNameField(
    userNameValue: MutableState<TextFieldValue>,
    isError: Boolean
) {
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
                capitalization = KeyboardCapitalization.Sentences
            ),
            isError = isError
        )
        if (isError) {
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
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
    ) {

        Button(
            onClick = { viewModel.signIn(userNameValue.value.text) },
            shape = CutCornerShape(5),
        ) {
            Text(stringResource(R.string.sign_in_button))
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewSignInScreen() {
    val vm: SignInViewModel = viewModel()
    MeshCommunicatorTheme {
        SignInScreen(vm)
    }
}
