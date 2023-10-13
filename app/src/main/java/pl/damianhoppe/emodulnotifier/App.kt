package pl.damianhoppe.emodulnotifier

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import android.util.Log
import androidx.compose.runtime.currentRecomposeScope
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import androidx.work.await
import com.google.android.material.color.DynamicColors
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import pl.damianhoppe.emodulnotifier.data.db.AppDatabase
import pl.damianhoppe.emodulnotifier.work.WorkManagerService
import java.util.UUID
import javax.inject.Inject
import kotlin.concurrent.thread

@HiltAndroidApp
class App : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    @Inject
    lateinit var workManager: WorkManagerService
    @Inject
    lateinit var database: AppDatabase

    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
        thread(start = true) {
            val all = database.moduleSettingsDao().getAll()
            all.forEach {
                if(it.fuelEmptyNotificationsEnabled)
                    workManager.enqueueFuelCheckWork(it.moduleId)
                if(it.pumpActivationScheduleEnabled)
                    workManager.enqueuePumpActivationWork(it)
                if(it.pumpShutdownScheduleEnabled)
                    workManager.enqueuePumpShutdownWork(it)
            }
        }
    }

    override fun getWorkManagerConfiguration() = Configuration.Builder()
        .setWorkerFactory(workerFactory).build()
}