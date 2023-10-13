package pl.damianhoppe.emodulnotifier.ui.login

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imeNestedScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.ViewModelProvider
import dagger.hilt.android.AndroidEntryPoint
import pl.damianhoppe.emodulnotifier.Navigator
import pl.damianhoppe.emodulnotifier.R
import pl.damianhoppe.emodulnotifier.ui.theme.EmodulNotifierTheme
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : ComponentActivity() {

    @Inject
    lateinit var navigator: Navigator

    private lateinit var viewModel: LoginViewModel

    @OptIn(ExperimentalLayoutApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.viewModel = ViewModelProvider(this)[LoginViewModel::class.java]

        viewModel.loginProcessingState.observe(this) {
            if (it == LoginViewModel.LoginProcessingState.SUCCESS) {
                navigator.openMainActivity()
            }
        }

        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            EmodulNotifierTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .imePadding()
                        .imeNestedScroll(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LoginForm(viewModel)
                }
            }
        }
    }
}

@Composable
fun LoginForm(viewModel: LoginViewModel) {
    val login by viewModel.userLogin.observeAsState("")
    val password by viewModel.userPassword.observeAsState("")
    val processingState by viewModel.loginProcessingState.observeAsState(null)
    var passwordVisible by remember() { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .padding(8.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        Text(stringResource(R.string.welcome),
        modifier = Modifier.fillMaxWidth(),
        fontSize = TextUnit(8f, TextUnitType.Em)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            if(processingState == LoginViewModel.LoginProcessingState.ERROR) stringResource(R.string.incorrect_login_password) else "",
            modifier = Modifier.fillMaxWidth(),
            color = Color(244, 67, 54, 255)
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = login,
            onValueChange = viewModel::onUserLoginChanged,
            label = { Text(stringResource(R.string.login)) },
            singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = password,
            onValueChange = viewModel::onUserPasswordChanged,
            label = { Text(stringResource(R.string.password)) },
            singleLine = true,
            visualTransformation = if(passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if(passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, "")
                }
            }
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = viewModel::login) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .wrapContentSize()
                    .animateContentSize()
            ) {
                if(processingState == LoginViewModel.LoginProcessingState.PROCESSING) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 3.dp,
                        modifier = Modifier
                            .size(20.dp)
                    )
                    Box(modifier = Modifier.width(12.dp))
                }
                Text(
                    stringResource(R.string.login),
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(vertical = 2.dp)
                )
            }
        }
    }
}