package pl.damianhoppe.emodulnotifier.work

import android.content.Context
import android.util.Log
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import pl.damianhoppe.emodulnotifier.App
import pl.damianhoppe.emodulnotifier.work.workers.FuelCheckWorker
import pl.damianhoppe.emodulnotifier.data.model.ModuleSettings
import pl.damianhoppe.emodulnotifier.work.workers.PumpActivationWorker
import pl.damianhoppe.emodulnotifier.work.workers.PumpShutdownWorker
import java.util.Calendar
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkManagerService @Inject constructor(@ApplicationContext val context: Context) {

    enum class WorkType {
        FuelCheck, PumpActivation, PumpShutdown
    }

    companion object {
        const val INPUT_DATA_MODULE_ID_KEY = "moduleId"
    }

    private val workManager: WorkManager;

    init {
        WorkManager.initialize(context, ((context) as App).workManagerConfiguration)
        this.workManager = WorkManager.getInstance(context)
    }

    fun generateModuleWorkId(workType: WorkType, moduleId: String): String = "${workType.name}-$moduleId"
    fun generateModuleWorkUUID(workType: WorkType, moduleId: String): UUID = UUID.nameUUIDFromBytes(generateModuleWorkId(workType, moduleId).toByteArray())

    fun enqueueFuelCheckWork(moduleId: String) {
        val workConstraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val work = PeriodicWorkRequestBuilder<FuelCheckWorker>(1, TimeUnit.HOURS)
            .setConstraints(workConstraints)
            .setId(generateModuleWorkUUID(WorkType.FuelCheck, moduleId))
            .setInputData(Data.Builder().putString(INPUT_DATA_MODULE_ID_KEY,moduleId).build())
            .build()
        workManager.enqueueUniquePeriodicWork(generateModuleWorkId(WorkType.FuelCheck, moduleId), ExistingPeriodicWorkPolicy.KEEP, work)
    }

    fun cancelFuelCheckWork(moduleId: String) = workManager.cancelWorkById(generateModuleWorkUUID(
        WorkType.FuelCheck, moduleId))

    fun enqueuePumpActivationWork(moduleSettings: ModuleSettings) {
        val workConstraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val work = PeriodicWorkRequestBuilder<PumpActivationWorker>(1, TimeUnit.DAYS)
            .setConstraints(workConstraints)
            .setId(generateModuleWorkUUID(WorkType.PumpActivation, moduleSettings.moduleId))
            .setInputData(Data.Builder().putString(INPUT_DATA_MODULE_ID_KEY,moduleSettings.moduleId).build())
            .setInitialDelay(calcDelay(now(), moduleSettings.pumpActivationTime),TimeUnit.MINUTES)
            .build()
        workManager.enqueueUniquePeriodicWork(generateModuleWorkId(WorkType.PumpActivation, moduleSettings.moduleId), ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE, work)
    }

    fun cancelPumpActivationWork(moduleId: String) = workManager.cancelWorkById(generateModuleWorkUUID(
        WorkType.PumpActivation, moduleId))

    fun enqueuePumpShutdownWork(moduleSettings: ModuleSettings) {
        val workConstraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val work = PeriodicWorkRequestBuilder<PumpShutdownWorker>(1, TimeUnit.DAYS)
            .setConstraints(workConstraints)
            .setId(generateModuleWorkUUID(WorkType.PumpShutdown, moduleSettings.moduleId))
            .setInputData(Data.Builder().putString(INPUT_DATA_MODULE_ID_KEY,moduleSettings.moduleId).build())
            .setInitialDelay(calcDelay(now(), moduleSettings.pumpShutdownTime),TimeUnit.MINUTES)
            .build()
        workManager.enqueueUniquePeriodicWork(generateModuleWorkId(WorkType.PumpShutdown, moduleSettings.moduleId), ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE, work)
    }

    fun cancelPumpShutdownWork(moduleId: String) = workManager.cancelWorkById(generateModuleWorkUUID(
        WorkType.PumpShutdown, moduleId))

    private fun calcDelay(t1: Int, t2: Int): Long {
        var diff = t2 - t1
        if(diff < 0)
            diff += 24*60
        return diff.toLong()
    }

    private fun now(): Int {
        val calendar = Calendar.getInstance()
        return calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
    }
}