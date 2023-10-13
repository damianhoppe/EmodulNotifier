package pl.damianhoppe.emodulnotifier.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import pl.damianhoppe.emodulnotifier.data.model.ModuleSettings

@Database(entities = [ModuleSettings::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun moduleSettingsDao(): ModuleSettingsDao
}