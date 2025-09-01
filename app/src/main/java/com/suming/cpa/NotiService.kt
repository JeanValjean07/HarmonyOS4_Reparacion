package com.suming.cpa

import android.service.notification.NotificationListenerService
import android.app.Notification
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.service.notification.StatusBarNotification
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class NotiService : NotificationListenerService() {


    companion object {
        const val ACTION_NOTIFICATION_RECEIVED = "com.example.study3.NOTIFICATION_RECEIVED"
        const val EXTRA_PACKAGE_NAME = "packagename"
        const val EXTRA_TITLE = "title"
        const val EXTRA_CONTENT = "content"
    }


    override fun onListenerConnected() {
        super.onListenerConnected()
        Handler(Looper.getMainLooper()).postDelayed({
            getNotiList()
        }, 100)

    }


    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        getNotiList()
    }


    fun getNotiList() {
        Handler(Looper.getMainLooper()).postDelayed({
            val activeNotifications = getActiveNotifications()
            if (activeNotifications != null) {
                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {

                    for (sbn in activeNotifications) {
                        delay(200)
                        val notification = sbn.notification
                        val extras = notification.extras
                        val title = extras.getString(Notification.EXTRA_TITLE, "")
                        val content = extras.getString(Notification.EXTRA_TEXT, "")
                        val packageName = sbn?.packageName ?: ""

                        val intent = Intent(ACTION_NOTIFICATION_RECEIVED)
                        intent.putExtra(EXTRA_PACKAGE_NAME, packageName)
                        intent.putExtra(EXTRA_TITLE, title)
                        intent.putExtra(EXTRA_CONTENT, content)
                        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
                    }

                }
            }

        }, 100)
    }
}


