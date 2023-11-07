package pl.damianhoppe.emodulnotifier.data.model

import androidx.core.math.MathUtils.clamp
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

const val DEFAULT_FUEL_LEVEL_THRESHOLD_FOR_NOTIFICATION: Int = 20
const val FUEL_LEVEL_MIN: Int = 0
const val FUEL_LEVEL_MAX: Int = 100

@Entity(tableName = "module_settings")
class ModuleSettings (

    @PrimaryKey(autoGenerate = true)
    var id: Long,
    val moduleId: String,
    var fuelEmptyNotificationsEnabled: Boolean = false,
    fuelLevelThresholdForNotification: Int = DEFAULT_FUEL_LEVEL_THRESHOLD_FOR_NOTIFICATION,
    var pumpActivationScheduleEnabled: Boolean = false,
    var pumpActivationTime: Int = 8*60,
    var pumpShutdownScheduleEnabled: Boolean = false,
    var pumpShutdownTime: Int = 20*60,
) {
    @ColumnInfo(defaultValue = "15")
    var fuelLevelThresholdForNotification: Int = fuelLevelThresholdForNotification
        set(value) {
            field = clamp(value, FUEL_LEVEL_MIN, FUEL_LEVEL_MAX)
        }

    companion object {
        fun Default(moduleId: String): ModuleSettings {
            return ModuleSettings(0, moduleId)
        }
    }
}