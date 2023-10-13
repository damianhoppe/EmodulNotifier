package pl.damianhoppe.emodulnotifier.data.emodul

import com.google.gson.JsonElement
import pl.damianhoppe.emodulnotifier.data.emodul.model.AuthenticateResults
import pl.damianhoppe.emodulnotifier.data.emodul.model.LoginForm
import pl.damianhoppe.emodulnotifier.data.emodul.model.Module
import pl.damianhoppe.emodulnotifier.data.emodul.model.Value
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface EmodulApi {

    @POST("api/v1/authentication")
    fun authenticate(@Body loginForm: LoginForm): Call<AuthenticateResults>

    @GET("api/v1/users/{userId}/modules")
    fun fetchModules(@Header("Authorization") authorization: String, @Path("userId") userId: String): Call<List<Module>>

    @GET("api/v1/users/{userId}/modules/{moduleId}")
    fun fetchModuleDetails(@Header("Authorization") authorization: String, @Path("userId") userId: String, @Path("moduleId") moduleId: String): Call<JsonElement>

    @POST("api/v1/users/{userId}/modules/{moduleId}/menu/MU/ido/2006")
    fun setPumpMode(@Header("Authorization") authorization: String, @Path("userId") userId: String, @Path("moduleId") moduleId: String, @Body value: Value<Int>): Call<JsonElement>
}