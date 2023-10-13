package pl.damianhoppe.emodulnotifier

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService
import dagger.hilt.android.qualifiers.ApplicationContext
import pl.damianhoppe.emodulnotifier.ui.start.StartActivity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationsService @Inject constructor(@ApplicationContext val context: Context) {

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(NotificationChannel("f", context.getString(R.string.notification_channel_name_fuel_check), NotificationManager.IMPORTANCE_DEFAULT))
            notificationManager.createNotificationChannel(NotificationChannel("p", context.getString(R.string.notification_channel_name_pump_mode_change), NotificationManager.IMPORTANCE_DEFAULT))
        }
    }

    fun showNotification(title: String, content: String, id: Int) {
        val builder = NotificationCompat.Builder(context, if(id == 0) "f" else "p")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(PendingIntent.getActivity(context, 0, Intent(context, StartActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }, PendingIntent.FLAG_IMMUTABLE))

        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            notify(id, builder.build())
        }

    }
}