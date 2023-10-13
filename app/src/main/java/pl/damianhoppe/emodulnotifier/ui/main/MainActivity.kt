package pl.damianhoppe.emodulnotifier.ui.main

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.TweenSpec
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Switch
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import dagger.hilt.android.AndroidEntryPoint
import pl.damianhoppe.emodulnotifier.Navigator
import pl.damianhoppe.emodulnotifier.R
import pl.damianhoppe.emodulnotifier.data.UserSessionStore
import pl.damianhoppe.emodulnotifier.data.model.ModuleWithSettings
import pl.damianhoppe.emodulnotifier.exceptions.UnAuthenticatedUserException
import pl.damianhoppe.emodulnotifier.ui.theme.EmodulNotifierTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var userSessionStore: UserSessionStore
    @Inject
    lateinit var navigator: Navigator

    lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        this.viewModel.modules.observe(this) {
            if(it!= null && it.isFailure && it.exceptionOrNull() is UnAuthenticatedUserException) {
                Toast.makeText(this@MainActivity, R.string.need_login_again, Toast.LENGTH_SHORT).show()
                logoutUser()
            }
        }

        setContent {
            EmodulNotifierTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainView(this)
                }
            }
        }
    }

    fun logoutUser() {
        viewModel.logout()
        navigator.openLoginActivity()
    }

    fun openTimePickerDialog(time: Int, callback: (Int) -> Unit) {
        val t = number2Time(time)
        val timePicker = MaterialTimePicker.Builder()
            .setHour(t.first)
            .setMinute(t.second)
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
            .build()
        timePicker .addOnPositiveButtonClickListener {
            callback(timePicker.hour*60 + timePicker.minute)
        }
        timePicker.show(supportFragmentManager, "TimePicker")
    }
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun MainView(activity: MainActivity) {
    val refreshing by activity.viewModel.refreshStatus.observeAsState()
    val modules by activity.viewModel.modules.observeAsState()

    val pullRefreshState = rememberPullRefreshState(refreshing = refreshing?: false, onRefresh = activity.viewModel::refresh)
    Box(modifier = Modifier
        .fillMaxSize()
        .pullRefresh(pullRefreshState)
    ) {
        AnimatedContent(
            targetState = modules,
            contentKey = { it?.isSuccess ?: false }
        ) {
            if(it == null) {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {}
            }else if(it.isFailure && it.exceptionOrNull() !is UnAuthenticatedUserException) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 28.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Image(
                        imageVector = Icons.Default.WifiOff,
                        contentDescription = "",
                        colorFilter = ColorFilter.tint(Color.White),
                    )
                    Text(
                        stringResource(R.string.modules_load_error),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        textAlign = TextAlign.Center,
                    )
                }
            }else {
                LazyColumn(
                    Modifier
                        .fillMaxSize()
                        .padding(top = 48.dp, start = 12.dp, end = 12.dp),
                    verticalArrangement = Arrangement.Center,
                ) {
                    items(
                        items = modules?.getOrDefault(emptyList()) ?: emptyList(),
                        key = { item -> item.settings.moduleId }
                    ) { item ->
//                        val alphaAnimated = remember(key1 = "visible-${item.settings.id}") { Animatable(0f) }
//                        LaunchedEffect("visible-${item.settings.id}") {
//                            alphaAnimated.animateTo(
//                                1f,
//                                animationSpec = TweenSpec(
//                                    durationMillis = 0
//                                )
//                            )
//                        }
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp, bottom = 8.dp)
                                /*.graphicsLayer {
                                    this.alpha = alphaAnimated.value
                                }*/,
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(24.dp),
                        ) {
                            ModuleSettingsView(activity, item)
                        }
                    }
                }
            }
        }
        LogoutButton(
            activity = activity,
            modifier = Modifier
                .align(Alignment.TopEnd),
        )
        PullRefreshIndicator(refreshing = refreshing?: false, state = pullRefreshState, Modifier.align(Alignment.TopCenter))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ModuleSettingsView(activity: MainActivity, moduleWithSettings: ModuleWithSettings) {
    val module = moduleWithSettings.module
    val settings = moduleWithSettings.settings

    var fuelEmptyAlert by remember(key1 = "1-${module.udid}") { mutableStateOf(settings.fuelEmptyNotificationsEnabled) }
    var pumpOn by remember(key1 = "2-${module.udid}") { mutableStateOf(settings.pumpActivationScheduleEnabled) }
    var pumpOnTime by remember(key1 = "3-${module.udid}") { mutableIntStateOf(settings.pumpActivationTime) }
    var pumpOff by remember(key1 = "4-${module.udid}") { mutableStateOf(settings.pumpShutdownScheduleEnabled) }
    var pumpOffTime by remember(key1 = "5-${module.udid}") { mutableIntStateOf(settings.pumpShutdownTime) }

    Column(
        modifier = Modifier
            .padding(vertical = 6.dp, horizontal = 12.dp)
            .padding(top = 12.dp),
    ) {
        Text(
            module.name,
            modifier = Modifier
                .fillMaxWidth(),
            fontSize = TextUnit(5.0f, TextUnitType.Em),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(R.string.notifiy_low_fuel_supply))
            Switch(
                checked = fuelEmptyAlert,
                onCheckedChange = {
                    settings.fuelEmptyNotificationsEnabled = it
                    fuelEmptyAlert = settings.fuelEmptyNotificationsEnabled
                    activity.viewModel.toggleFuelEmptyNotificationsFor(moduleWithSettings)
                }
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(R.string.activate_parallel_pumps))
            Switch(
                checked = pumpOn,
                onCheckedChange = {
                    settings.pumpActivationScheduleEnabled = it
                    pumpOn = settings.pumpActivationScheduleEnabled
                    activity.viewModel.updateParallelPumpSchedule(moduleWithSettings)
                }
            )
        }
        TextButton(
            modifier = Modifier
                .align(Alignment.End)
                .graphicsLayer {
                    alpha = if(pumpOn) 1f else 0.4f
                },
            onClick = { activity.openTimePickerDialog(pumpOnTime) {
                settings.pumpActivationTime = it
                pumpOnTime = settings.pumpActivationTime
                activity.viewModel.updateParallelPumpSchedule(moduleWithSettings)
            } }
        ) {
            Text(number2StringTime(pumpOnTime))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(stringResource(R.string.activate_summer_mode))
            Switch(
                checked = pumpOff,
                onCheckedChange = {
                    settings.pumpShutdownScheduleEnabled = it
                    pumpOff = settings.pumpShutdownScheduleEnabled
                    activity.viewModel.updateSummerPumpModeSchedule(moduleWithSettings)
                }
            )
        }
        TextButton(
            modifier = Modifier
                .align(Alignment.End)
                .graphicsLayer {
                    alpha = if(pumpOff) 1f else 0.4f
                },
            onClick = { activity.openTimePickerDialog(pumpOffTime) {
                settings.pumpShutdownTime = it
                pumpOffTime = settings.pumpShutdownTime
                activity.viewModel.updateSummerPumpModeSchedule(moduleWithSettings)
            } }
        ) {
            Text(number2StringTime(pumpOffTime))
        }
    }
}

fun number2Time(time: Int): Pair<Int, Int> {
    return Pair(time / 60, time % 60)
}

fun number2StringTime(time: Int): String {
    val hour = int2DoubleNumberString(time / 60)
    val minute = int2DoubleNumberString(time % 60)
    return "$hour:$minute"
}

fun int2DoubleNumberString(value: Int): String {
    if(value < 10)
        return "0$value"
    return "$value"
}

@Composable
fun LogoutButton(activity: MainActivity, modifier: Modifier) {
    TextButton(
        modifier = modifier,
        onClick = {
            activity.logoutUser()
        }
    ) {
        Text(text = stringResource(R.string.logout), fontSize = TextUnit(3.2f, TextUnitType.Em), fontWeight = FontWeight.Bold)
    }
}