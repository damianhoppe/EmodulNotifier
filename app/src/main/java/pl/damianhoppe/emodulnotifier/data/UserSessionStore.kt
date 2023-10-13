package pl.damianhoppe.emodulnotifier.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import pl.damianhoppe.emodulnotifier.data.model.UserSession
import javax.inject.Inject

class UserSessionStore @Inject constructor(@ApplicationContext private val context: Context) {
    companion object {
        private val Context.userSessionStore: DataStore<Preferences> by preferencesDataStore("userSessionStore")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val USER_TOKEN_KEY = stringPreferencesKey("user_token")
    }

    private val getUserId: Flow<String?> = context.userSessionStore.data.map { preferences -> preferences[USER_ID_KEY] }
    private val getUserToken: Flow<String?> = context.userSessionStore.data.map { preferences -> preferences[USER_TOKEN_KEY] }

    val getUserSession: Flow<UserSession?>  = getUserId.combine(getUserToken) { userId, userToken ->
        if (userId != null && userToken != null)
            return@combine UserSession(userId, userToken)
        return@combine null;
    }

    suspend fun saveUserSession(userSession: UserSession) {
        context.userSessionStore.edit { preferences ->
            preferences[USER_ID_KEY] = userSession.userId
            preferences[USER_TOKEN_KEY] = userSession.token
        }
    }

    suspend fun invalidateUserSession() {
        context.userSessionStore.edit { preferences ->
            preferences.remove(USER_ID_KEY)
            preferences.remove(USER_TOKEN_KEY)
        }
    }
}