package pl.damianhoppe.emodulnotifier.data.model

const val DEMO_USER_ID = "demoSpecialAccount"
const val DEMO_USER_PASSWORD = "demo"

fun UserSession?.isDemoAccount(): Boolean = this != null && this.userId == DEMO_USER_ID

fun getDemoSession() = UserSession(DEMO_USER_ID, DEMO_USER_ID)

data class UserSession(
    val userId: String,
    val token: String
)
