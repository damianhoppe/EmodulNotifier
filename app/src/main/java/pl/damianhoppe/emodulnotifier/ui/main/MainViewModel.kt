package pl.damianhoppe.emodulnotifier.ui.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import pl.damianhoppe.emodulnotifier.work.WorkManagerService
import pl.damianhoppe.emodulnotifier.data.UserSessionStore
import pl.damianhoppe.emodulnotifier.data.db.AppDatabase
import pl.damianhoppe.emodulnotifier.data.emodul.EmodulApi
import pl.damianhoppe.emodulnotifier.data.emodul.model.Module
import pl.damianhoppe.emodulnotifier.data.model.ModuleSettings
import pl.damianhoppe.emodulnotifier.data.model.ModuleWithSettings
import pl.damianhoppe.emodulnotifier.exceptions.requireUserAuthenticated
import pl.damianhoppe.emodulnotifier.utils.AuthorizationHeader
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userSessionStore: UserSessionStore,
    private val emodulApi: EmodulApi,
    private val database: AppDatabase,
    private val workManagerService: WorkManagerService,
) : ViewModel() {

    val modules: MutableLiveData<Result<List<ModuleWithSettings>>> = MutableLiveData<Result<List<ModuleWithSettings>>>(null)
    val refreshStatus: MutableLiveData<Boolean> = MutableLiveData<Boolean>(false)

    init {
        refresh()
    }

    fun refresh() {
        if(refreshStatus.value == true)
            return
        refreshStatus.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                modules.postValue(Result.success(loadModules()))
            }catch (e: Exception) {
                modules.postValue(Result.failure(e))
            }
            viewModelScope.launch { refreshStatus.value = false }
        }
    }

    private suspend fun loadModules(): List<ModuleWithSettings> {
        var modulesFromApi: List<Module>
        val userSession =
            userSessionStore.getUserSession.firstOrNull() ?: throw IllegalStateException();
        val result = emodulApi.fetchModules(
            AuthorizationHeader.Bearer(userSession.token),
            userSession.userId
        ).execute()
        requireUserAuthenticated(result)

        modulesFromApi = result.body()!!
        val moduleSettingsDao = database.moduleSettingsDao()
        val savedModuleSettings = moduleSettingsDao.getAll().toMutableList()
        val modulesWithSettings = modulesFromApi.map {
            val index = savedModuleSettings.indexOfFirst { moduleSettings ->  moduleSettings.moduleId == it.udid }
            val moduleSettings: ModuleSettings?
            if(index >= 0) {
                moduleSettings = savedModuleSettings[index]
                savedModuleSettings.removeAt(index)
                return@map ModuleWithSettings(it, moduleSettings)
            }
            moduleSettings = ModuleSettings.Default(it.udid)
            moduleSettings.id = moduleSettingsDao.insert(moduleSettings)
            ModuleWithSettings(it, moduleSettings)
        }
        savedModuleSettings.forEach {
            moduleSettingsDao.delete(it)
        }
        return modulesWithSettings
    }

    fun logout() {
        viewModelScope.launch(Dispatchers.IO) {
            userSessionStore.invalidateUserSession()
            val modules = database.moduleSettingsDao().getAll()
            modules.forEach {
                workManagerService.cancelFuelCheckWork(it.moduleId)
                workManagerService.cancelPumpActivationWork(it.moduleId)
                workManagerService.cancelPumpShutdownWork(it.moduleId)
                database.moduleSettingsDao().delete(it)
            }
        }
    }

    fun toggleFuelEmptyNotificationsFor(moduleWithSettings: ModuleWithSettings) {
        viewModelScope.launch(Dispatchers.IO) {
            database.moduleSettingsDao().update(moduleWithSettings.settings)
        }
        if(moduleWithSettings.settings.fuelEmptyNotificationsEnabled)
            workManagerService.enqueueFuelCheckWork(moduleWithSettings.module.udid)
        else
            workManagerService.cancelFuelCheckWork(moduleWithSettings.module.udid)
    }

    fun updateParallelPumpSchedule(moduleWithSettings: ModuleWithSettings) {
        viewModelScope.launch(Dispatchers.IO) {
            database.moduleSettingsDao().update(moduleWithSettings.settings)
        }
        if(moduleWithSettings.settings.pumpActivationScheduleEnabled)
            workManagerService.enqueuePumpActivationWork(moduleWithSettings.settings)
        else
            workManagerService.cancelPumpActivationWork(moduleWithSettings.module.udid)
    }

    fun updateSummerPumpModeSchedule(moduleWithSettings: ModuleWithSettings) {
        viewModelScope.launch(Dispatchers.IO) {
            database.moduleSettingsDao().update(moduleWithSettings.settings)
        }
        if(moduleWithSettings.settings.pumpShutdownScheduleEnabled)
            workManagerService.enqueuePumpShutdownWork(moduleWithSettings.settings)
        else
            workManagerService.cancelPumpShutdownWork(moduleWithSettings.module.udid)
    }
}