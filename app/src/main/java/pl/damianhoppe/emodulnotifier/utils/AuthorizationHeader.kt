package pl.damianhoppe.emodulnotifier.utils

class AuthorizationHeader {

    companion object {
        fun Bearer(token: String) = "Bearer $token"
    }
}