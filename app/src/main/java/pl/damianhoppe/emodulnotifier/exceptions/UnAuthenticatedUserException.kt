package pl.damianhoppe.emodulnotifier.exceptions

import retrofit2.Response

@Throws(UnAuthenticatedUserException::class)
fun <T> requireUserAuthenticated(httpResponse: Response<T>) {
    if(httpResponse.code() == 401)
        throw UnAuthenticatedUserException()
}

class UnAuthenticatedUserException: Exception()