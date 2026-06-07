package com.suming.reparacion.ActivityComponents

import android.app.Notification
import android.content.Intent
import android.os.IBinder
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.compose.ui.input.key.Key
import com.suming.reparacion.DataPack.NotificationPack
import com.suming.reparacion.SettingsRequestCenter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.UUID

class NotificationManagerReceiver : NotificationListenerService() {
    companion object {
        const val ACTION_NOTIFICATION_RECEIVED = "NOTIFICATION_RECEIVED"
        const val EXTRA_PACKAGE_NAME = "packagename"
        const val EXTRA_TITLE = "title"
        const val EXTRA_CONTENT = "content"
    }

    private val coroutine_observe = CoroutineScope(Dispatchers.Main)

    //LifeCycle
    //服务连接
    override fun onListenerConnected() {
        super.onListenerConnected()
        consoleLog("通知监听服务：成功连接到通知监听器服务,开始遍历当前通知")
        NotificationManagerRepo.setIsServiceOnline(true)
        //连接时获取所有已存在通知
        fetchAndStoreAllNotifications()
        //观察服务状态位
        coroutine_observe.launch {
            NotificationManagerRepo.service_connect.collect { newValue ->
                consoleLog("服务连接状态变为: $newValue")
                when(newValue) {
                    "SERVICE_INTENT_FETCH_ALL" -> {
                        fetchAndStoreAllNotifications()
                    }
                    "SERVICE_INTENT_GATHER_CURRENT" -> {
                        fetchAndStoreAllNotifications()
                    }
                    "SERVICE_INTENT_CANCEL" -> {
                        NotificationManagerRepo.clearServiceConnect()
                        val key = NotificationManagerRepo.getNeedCancelKey()
                        consoleLog("通知监听服务：收到取消通知指令 key $key")
                        cancelNotification(key)
                    }
                    "SERVICE_INTENT_SNOOZE" -> {
                        NotificationManagerRepo.clearServiceConnect()
                        val key = NotificationManagerRepo.getNeedSnoozeKey()
                        consoleLog("通知监听服务：收到隐藏通知指令 key $key")
                        snoozeNotificationByKey(key, 2_592_000_000L)
                    }
                    "SERVICE_INTENT_DELAY_SECONDS" -> {
                        NotificationManagerRepo.clearServiceConnect()
                        val content = NotificationManagerRepo.getNeedDelayKey()
                        consoleLog("通知监听服务：收到延后通知指令 key $content")
                        delayNotificationByKey(content.first, content.second)
                    }
                }
            }
        }


    }
    //服务断开
    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        consoleLog("通知监听服务：已断开连接")
        coroutine_observe.cancel()
    }
    override fun onDestroy() {
        super.onDestroy()
        consoleLog("通知监听服务：已被销毁")
        coroutine_observe.cancel()
    }
    //收到新的通知
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if(sbn == null) {
            consoleLog("通知监听服务：收到一条空的通知")
        }else{
            consoleLog("通知监听服务：收到一条通知")
            //提取通知元数据
            val packageName = sbn.packageName
            val postTime = sbn.postTime
            val isOngoing = sbn.isOngoing
            val key = sbn.key
            val notification = sbn.notification
            val title = notification?.extras?.getString(Notification.EXTRA_TITLE, "") ?: ""
            val text = notification?.extras?.getString(Notification.EXTRA_TEXT, "") ?: ""
            consoleLog("通知监听服务：收到一条新通知：包名：$packageName，标题：$title，内容：$text，是否持续：$isOngoing，发布时间：$postTime")
            //内容为空时可以忽略(预留设置分支,设置项：保留内容为空的通知)
            val shouldAdd = if(SettingsRequestCenter.get_PREFS_Notification_Keep_Empty(this)){
                if (text.isEmpty() && title.isEmpty()){
                    false
                }else{
                    true
                }
            }else{
                true
            }
            //创建通知数据包
            if (shouldAdd){
                //创建通知数据包
                val notificationPack = NotificationPack(
                    uniqueID = UUID.randomUUID().toString(),
                    key = key,
                    packageName = packageName,
                    postTime = postTime,
                    isOngoing = isOngoing,
                    title = title,
                    text = text,
                )
                //添加到仓库
                NotificationManagerRepo.add(notificationPack)
            }

        }

    }



    //Functions
    //获取所有通知
    fun fetchAndStoreAllNotifications(){
        val activeNotifications = getActiveNotifications()
        if (activeNotifications != null) {
            for (sbn in activeNotifications) {
                val uniqueID = UUID.randomUUID().toString()
                /*
                if (lastMillis == 0L){
                    lastMillis = System.currentTimeMillis()
                    uniqueID = lastMillis
                }else{
                    val currentMillis = System.currentTimeMillis()
                    if (currentMillis == lastMillis){
                        uniqueID = currentMillis * 10 + sameMillisCount
                        sameMillisCount++
                    }else{
                        sameMillisCount = 0
                        lastMillis = currentMillis
                        uniqueID = currentMillis
                    }
                }
                 private var lastMillis = 0L
                private var sameMillisCount = 0
                 */
                val packageName = sbn.packageName
                val postTime = sbn.postTime
                val isOngoing = sbn.isOngoing
                val key = sbn.key
                val notification = sbn.notification
                val title = notification?.extras?.getString(Notification.EXTRA_TITLE, "") ?: ""
                val text = notification?.extras?.getString(Notification.EXTRA_TEXT, "") ?: ""
                consoleLog("通知监听服务：遍历通知：ID:$uniqueID，包名：$packageName，标题：$title，内容：$text，是否持续：$isOngoing，发布时间：$postTime")
                //内容为空时可以忽略(预留设置分支,设置项：保留内容为空的通知)
                val shouldAdd = if(SettingsRequestCenter.get_PREFS_Notification_Keep_Empty(applicationContext)){
                    //默认丢弃内容为空的通知
                    if (text.isEmpty() && title.isEmpty()){
                        false
                    }else{
                        true
                    }
                }else{
                    true
                }
                //创建通知数据包
                if (shouldAdd){
                    val notificationPack = NotificationPack(
                        uniqueID = uniqueID,
                        key = key,
                        packageName = packageName,
                        postTime = postTime,
                        isOngoing = isOngoing,
                        title = title,
                        text = text,
                    )
                    //添加到仓库
                    NotificationManagerRepo.add(notificationPack)
                    //刷新状态
                    NotificationManagerRepo.updateCount()
                    NotificationManagerRepo.setServiceConnect("")
                    NotificationManagerRepo.updateErrorCoverMark()
                }
            }
        }
    }
    //取消通知
    fun cancelNotification(key: Key) {
        cancelNotification(key)

    }
    //隐藏通知(极端延后)
    fun snoozeNotificationByKey(key: String, delayMillis: Long) {
        snoozeNotification(key, delayMillis)
    }
    //延后通知(注意:传入是秒，需要转毫秒)
    fun delayNotificationByKey(key: String,seconds: Int) {
        snoozeNotification(key, (seconds * 1000).toLong())
    }


    //统一日志控制
    private fun consoleLog(msg: String, mark: Boolean = true) {
        if (mark) {
            Log.d("SuMing", msg)
        }
    }

}