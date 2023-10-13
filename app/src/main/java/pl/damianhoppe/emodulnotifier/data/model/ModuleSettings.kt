package pl.damianhoppe.emodulnotifier.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "module_settings")
class ModuleSettings (

    @PrimaryKey(autoGenerate = true)
    var id: Long,
    val moduleId: String,
    var fuelEmptyNotificationsEnabled: Boolean,
    var pumpActivationScheduleEnabled: Boolean,
    var pumpActivationTime: Int,
    var pumpShutdownScheduleEnabled: Boolean,
    var pumpShutdownTime: Int,
) {
    companion object {
        fun Default(moduleId: String): ModuleSettings {
            return ModuleSettings(0, moduleId,false,false,8*60,false,20*60)
        }
    }
}