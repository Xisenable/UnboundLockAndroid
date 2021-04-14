package hrw.es.unboundlock

import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.view.View
import androidx.core.app.NotificationCompat

class Notification {

    lateinit var notificationManager: NotificationManager
    lateinit var notificationChannel: NotificationChannel
    lateinit var notificationBuilder: Notification.Builder
    private val channelID = "hrw.es.unboundlock"
    private val description = "UnboundLock Notifications"

    fun create(notificationManager2: NotificationManager) {
        this.notificationManager = notificationManager2

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = NotificationChannel(channelID, description, NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.GREEN
            notificationChannel.enableVibration(true)
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    fun displayNotification(title: String, text: String, view: View) {
        val builder = NotificationCompat.Builder(view.context, channelID)
                .setSmallIcon(R.drawable.notification)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setLargeIcon(BitmapFactory.decodeResource(view.resources, R.drawable.notification))

        if (isAppIsInBackground(view.context))
            notificationManager.notify(23, builder.build())
    }

    /**
     * Method checks if the app is in background or not
     * https://stackoverflow.com/questions/42312167/notification-android-dont-show-notification-when-app-is-open
     */
    private fun isAppIsInBackground(context: Context): Boolean {
        var isInBackground = true
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val runningProcesses = am.runningAppProcesses
            for (processInfo in runningProcesses) {
                if (processInfo.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (activeProcess in processInfo.pkgList) {
                        if (activeProcess == context.getPackageName()) {
                            isInBackground = false
                        }
                    }
                }
            }
        } else {
            val taskInfo = am.getRunningTasks(1)
            val componentInfo = taskInfo[0].topActivity
            if (componentInfo!!.packageName == context.getPackageName()) {
                isInBackground = false
            }
        }
        return isInBackground
    }

}