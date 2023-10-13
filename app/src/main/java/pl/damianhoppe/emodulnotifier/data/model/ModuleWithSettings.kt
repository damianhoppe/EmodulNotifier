package pl.damianhoppe.emodulnotifier.data.model

import pl.damianhoppe.emodulnotifier.data.emodul.model.Module

data class ModuleWithSettings(
    val module: Module,
    val settings: ModuleSettings
)