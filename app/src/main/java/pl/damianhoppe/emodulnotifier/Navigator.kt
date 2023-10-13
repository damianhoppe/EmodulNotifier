package pl.damianhoppe.emodulnotifier

import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.qualifiers.ApplicationContext
import pl.damianhoppe.emodulnotifier.ui.login.LoginActivity
import pl.damianhoppe.emodulnotifier.ui.main.MainActivity
import pl.damianhoppe.emodulnotifier.ui.start.StartActivity
import javax.inject.Inject

class Navigator @Inject constructor(
    @ActivityContext private val context: Context
    ) {

    fun openLoginActivity() {
        val intent = Intent(context, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(intent)
    }

    fun openMainActivity() {
        val intent = Intent(context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(intent)
    }
}