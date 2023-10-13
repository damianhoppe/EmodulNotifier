package pl.damianhoppe.emodulnotifier.data.emodul.model

data class AuthenticateResults(
    val authenticated: Boolean,
    val user_id: String,
    val token: String,
)