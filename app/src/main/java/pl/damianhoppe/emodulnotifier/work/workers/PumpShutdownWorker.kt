package pl.damianhoppe.emodulnotifier.work.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import pl.damianhoppe.emodulnotifier.NotificationsService
import pl.damianhoppe.emodulnotifier.R
import pl.damianhoppe.emodulnotifier.data.UserSessionStore
import pl.damianhoppe.emodulnotifier.data.emodul.EmodulApi
import pl.damianhoppe.emodulnotifier.data.emodul.model.PumpModes
import pl.damianhoppe.emodulnotifier.data.emodul.model.Value
import pl.damianhoppe.emodulnotifier.utils.AuthorizationHeader
import pl.damianhoppe.emodulnotifier.work.WorkManagerService

@HiltWorker
class PumpShutdownWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val emodulApi: EmodulApi,
    private val notificationsService: NotificationsService,
    private val userSessionStore: UserSessionStore,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val moduleId: String = this.inputData.getString(WorkManagerService.INPUT_DATA_MODULE_ID_KEY)!!
        val userSession = userSessionStore.getUserSession.first()!!

        try {
            emodulApi.setPumpMode(
                AuthorizationHeader.Bearer(userSession.token), userSession.userId, moduleId, Value(
                    PumpModes.PUMP_SUMMER_MODE
                )
            ).execute()
            notificationsService.showNotification(applicationContext.getString(R.string.pump_mode_changed),applicationContext.getString(
                R.string.summer_mode_activated),2)
        }catch (_: Exception) {
            notificationsService.showNotification(applicationContext.getString(R.string.pump_mode_changed),applicationContext.getString(R.string.summer_mode_activation_failed),2)
        }
        return Result.success()
    }
}