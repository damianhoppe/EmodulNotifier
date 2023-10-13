package pl.damianhoppe.emodulnotifier.work.workers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import okhttp3.internal.http.ExchangeCodec
import pl.damianhoppe.emodulnotifier.NotificationsService
import pl.damianhoppe.emodulnotifier.R
import pl.damianhoppe.emodulnotifier.data.UserSessionStore
import pl.damianhoppe.emodulnotifier.data.emodul.EmodulApiService
import pl.damianhoppe.emodulnotifier.utils.AuthorizationHeader
import pl.damianhoppe.emodulnotifier.work.WorkManagerService

@HiltWorker
class FuelCheckWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val emodulApiService: EmodulApiService,
    private val notificationsService: NotificationsService,
    private val userSessionStore: UserSessionStore,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val moduleId: String = this.inputData.getString(WorkManagerService.INPUT_DATA_MODULE_ID_KEY)!!
        val userSession = userSessionStore.getUserSession.first()!!
        val fuelSupply: Int
        try {
            fuelSupply =  emodulApiService.getFuelSupplyLevel(userSession.token, userSession.userId, moduleId)
        }catch (e: Exception) {
            notificationsService.showNotification(applicationContext.getString(R.string.fuel_supply_level), applicationContext.getString(R.string.fuel_supply_error), 0)
            return Result.failure();
        }
        if(fuelSupply <= 15) {
            notificationsService.showNotification(applicationContext.getString(R.string.fuel_supply_level), "$fuelSupply%", 0)
        }
        return Result.success()
    }
}