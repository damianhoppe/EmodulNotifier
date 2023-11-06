package pl.damianhoppe.emodulnotifier.ui.login

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.damianhoppe.emodulnotifier.data.UserSessionStore
import pl.damianhoppe.emodulnotifier.data.emodul.EmodulApi
import pl.damianhoppe.emodulnotifier.data.emodul.model.LoginForm
import pl.damianhoppe.emodulnotifier.data.model.DEMO_USER_ID
import pl.damianhoppe.emodulnotifier.data.model.DEMO_USER_PASSWORD
import pl.damianhoppe.emodulnotifier.data.model.UserSession
import pl.damianhoppe.emodulnotifier.data.model.getDemoSession
import java.lang.Exception
import java.lang.IllegalStateException
import java.lang.NullPointerException
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(): ViewModel() {

    enum class LoginProcessingState {
        PROCESSING, ERROR, SUCCESS
    }

    @Inject
    lateinit var userSessionStore: UserSessionStore
    @Inject
    lateinit var emodulApi: EmodulApi

    private var _userLogin = MutableLiveData("")
    private var _userPassword = MutableLiveData("")
    private var _loginProcessingState = MutableLiveData<LoginProcessingState>(null)

    val userLogin
        get(): LiveData<String> = _userLogin
    val userPassword
        get(): LiveData<String> = _userPassword
    val loginProcessingState
        get(): LiveData<LoginProcessingState> = _loginProcessingState

    fun onUserLoginChanged(userLogin: String) {
        this._userLogin.value = userLogin
    }

    fun onUserPasswordChanged(userPassword: String) {
        this._userPassword.value = userPassword
    }

    fun login() {
        if(_loginProcessingState.value == LoginProcessingState.PROCESSING || _loginProcessingState.value == LoginProcessingState.SUCCESS)
            return
        _loginProcessingState.value = LoginProcessingState.PROCESSING
        viewModelScope.launch(Dispatchers.IO) {
            try {
                delay(2000)
                if(_userLogin.value.isNullOrBlank() || _userPassword.value.isNullOrBlank())
                    throw Exception("Incorrect login or password")
                val userSession: UserSession
                if(_userLogin.value == DEMO_USER_ID && _userPassword.value == DEMO_USER_PASSWORD) {
                    userSession = getDemoSession()
                }else {
                    val result = emodulApi.authenticate(LoginForm(_userLogin.value!!, _userPassword.value!!)).execute().body()
                    if(result == null || !result.authenticated)
                        throw Exception("Incorrect login or password")
                    userSession = UserSession(result.user_id, result.token)
                }
                userSessionStore.saveUserSession(userSession)
                viewModelScope.launch {
                    _loginProcessingState.value = LoginProcessingState.SUCCESS
                }
            }catch (e: Exception) {
                e.printStackTrace()
                viewModelScope.launch {
                    _loginProcessingState.value = LoginProcessingState.ERROR
                }
            }
        }
    }
}