package pl.damianhoppe.emodulnotifier.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import pl.damianhoppe.emodulnotifier.data.model.ModuleSettings

@Dao
interface ModuleSettingsDao {

    @Query("SELECT * FROM module_settings")
    fun getAll(): List<ModuleSettings>

    @Update
    fun update(moduleSettings: ModuleSettings)

    @Insert
    fun insert(moduleSettings: ModuleSettings): Long

    @Delete
    fun delete(moduleSettings: ModuleSettings)
}