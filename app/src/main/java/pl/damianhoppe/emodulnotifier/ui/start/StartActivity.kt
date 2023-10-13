package pl.damianhoppe.emodulnotifier.ui.start

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import pl.damianhoppe.emodulnotifier.Navigator
import pl.damianhoppe.emodulnotifier.data.UserSessionStore
import pl.damianhoppe.emodulnotifier.data.model.UserSession
import pl.damianhoppe.emodulnotifier.ui.theme.EmodulNotifierTheme
import javax.inject.Inject

@AndroidEntryPoint
class StartActivity : ComponentActivity() {

    @Inject
    lateinit var userSessionStore: UserSessionStore
    @Inject
    lateinit var navigator: Navigator

    var permissionsChecked = false
    var userSession: UserSession? = null
    var userSessionLoaded = false

    val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            permissionsChecked = true
            tryGoToNextActivity()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }else {
            permissionsChecked = true
        }

        lifecycleScope.launch(Dispatchers.IO) {
            userSession = userSessionStore.getUserSession.firstOrNull()
            userSessionLoaded = true
            tryGoToNextActivity()
        }

        setContent {
            EmodulNotifierTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }

    fun tryGoToNextActivity() {
        if(!permissionsChecked || !userSessionLoaded)
            return
        if(userSession == null)
            runOnUiThread { navigator.openLoginActivity() }
        else
            runOnUiThread { navigator.openMainActivity() }
    }
}