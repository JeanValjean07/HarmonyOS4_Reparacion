package com.suming.reparacion.ActivityComponents.NotificationManager

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.compose.ui.input.key.Key
import com.suming.reparacion.ActivityComponents.NotificationManager.NotificationManagerRepo
import com.suming.reparacion.DataPack.Connect
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


    //LifeCycle
    //服务连接
    override fun onListenerConnected() {
        super.onListenerConnected()
        consoleLog("通知监听服务：成功连接到通知监听器服务,开始遍历当前通知")
        NotificationManagerRepo.setIsServiceOnline(true)
        //连接时获取所有已存在的活动通知
        HandleActiveNotification()
        //开始观察服务状态位
        startObserve()
    }
    //服务断开
    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        consoleLog("通知监听服务：已断开连接")
        stopObserve()
    }
    override fun onDestroy() {
        super.onDestroy()
        consoleLog("通知监听服务：已被销毁")
        stopObserve()
    }
    //收到新的通知
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        //处理新通知
        if(sbn == null) {
            consoleLog("通知监听服务：收到一条空的通知")
        }else{
            consoleLog("通知监听服务：收到一条通知")
            HandleNewPostedNotification(sbn)
        }

    }



    //Functions
    private val coroutine_observe = CoroutineScope(Dispatchers.Main)
    private fun startObserve(){
        coroutine_observe.launch {
            NotificationManagerRepo.service_connect.collect { newValue ->
                if(newValue != ""){
                    consoleLog("通知监听服务观察者：服务连接状态变为: $newValue")
                    NotificationManagerRepo.clearServiceConnect()
                }
                when(newValue) {
                    Connect.service_intent_fetch_all -> {
                        consoleLog("通知监听服务观察者：收到 获取所有通知 指令")
                        HandleActiveNotification()
                    }
                    Connect.service_intent_cancel -> {
                        val key = NotificationManagerRepo.getNeedCancelKey()
                        consoleLog("通知监听服务观察者：收到取消通知指令 key $key")
                        cancelNotification(key)
                    }
                    Connect.service_intent_snooze -> {
                        val key = NotificationManagerRepo.getNeedSnoozeKey()
                        consoleLog("通知监听服务观察者：收到隐藏通知指令 key $key")
                        snoozeNotificationByKey(key)
                    }
                    Connect.service_intent_delay_seconds -> {
                        val content = NotificationManagerRepo.getNeedDelayKey()
                        consoleLog("通知监听服务观察者：收到延后通知指令 key ${content.first} seconds ${content.second}")
                        delayNotificationByKey(content.first, content.second)
                    }
                    Connect.service_intent_get_delay_list -> {
                        fetchAndStoreSnoozedNotifications()
                    }
                    Connect.service_intent_delay_cancel -> {
                        val key = NotificationManagerRepo.getNeedCancelDelayKey()
                        consoleLog("通知监听服务观察者：收到取消延后通知指令 key ${key} ")
                        delayNotificationByKey(key, 1)
                    }
                }
            }
        }
    }
    private fun stopObserve(){
        coroutine_observe.cancel()
    }

    //获取已被延时的通知
    private fun fetchAndStoreSnoozedNotifications() {
        consoleLog("通知监听服务：开始执行 获取已被延时的通知")
        val snoozedNotifications = getSnoozedNotifications()
        if (snoozedNotifications != null) {
            for (sbn in snoozedNotifications) {
                val uniqueID = UUID.randomUUID().toString()
                val packageName = sbn.packageName
                val postTime = sbn.postTime
                val isOngoing = sbn.isOngoing
                val key = sbn.key
                val notification = sbn.notification
                val title = notification?.extras?.getString(Notification.EXTRA_TITLE, "") ?: ""
                val text = notification?.extras?.getString(Notification.EXTRA_TEXT, "") ?: ""
                consoleLog("通知监听服务：遍历通知：ID:$uniqueID，包名：$packageName，标题：$title，内容：$text，是否持续：$isOngoing，发布时间：$postTime")
                //创建通知数据包
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
                NotificationManagerRepo.addDelay(notificationPack)
            }
        }
    }
    //取消通知
    private fun cancelNotification(key: Key) {
        consoleLog("通知监听服务：取消通知： key $key")
        cancelNotification(key)
    }
    //隐藏通知(极端延后)
    private fun snoozeNotificationByKey(key: String) {
        consoleLog("通知监听服务：隐藏通知指令 key $key")
        snoozeNotification(key, 2_592_000_000L)
    }
    //延后通知(注意:传入是秒，需要转毫秒)
    private fun delayNotificationByKey(key: String,seconds: Int) {
        consoleLog("通知监听服务：收到延后通知指令 key $key, seconds $seconds")
        snoozeNotification(key, (seconds * 1000).toLong())
    }


    //处理新通知
    private val coroutine_get_active = CoroutineScope(Dispatchers.IO)
    private fun HandleActiveNotification(){
        coroutine_get_active.launch {
            fetchAndStoreActiveNotifications()
        }
    }
    //处理当前活跃的通知
    private val coroutine_get_new_posted = CoroutineScope(Dispatchers.IO)
    private fun HandleNewPostedNotification(sbn: StatusBarNotification){
        coroutine_get_new_posted.launch{
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
            val shouldAdd = getEmptyNotificationMark(text,title)
            //创建通知数据包并添加
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
            //刷新状态
            stateUpdate()
        }
    }

    //获取所有活动通知
    private fun fetchAndStoreActiveNotifications(){
        consoleLog("通知监听服务：开始执行 获取所有活动通知")
        val activeNotifications = getActiveNotifications()
        if (activeNotifications != null){
            processNotificationPack(activeNotifications)
        }
    }
    //处理通知数据包
    private fun processNotificationPack(notifications: Array<StatusBarNotification>){
        for (sbn in notifications) {
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
            //内容为空时可以忽略
            val shouldAdd = getEmptyNotificationMark(text, title)
            //创建通知数据包并添加到仓库
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

            }

            //刷新状态
            stateUpdate()
        }

    }
    //集中状态刷新(包括活跃通知的数量,错误状态)
    private fun stateUpdate(){
        NotificationManagerRepo.updateCount()
        NotificationManagerRepo.updateErrorCoverMark()
    }
    //添加空通知标记
    private fun getEmptyNotificationMark(text: String, title: String) : Boolean{
        if(SettingsRequestCenter.get_PREFS_Notification_Keep_Empty(applicationContext)){
            //保留内容为空的通知(也就是全部保留)
            return true
        }else{
            //过滤掉内容为空的通知
            if (text.isEmpty() && title.isEmpty()){
                return false
            }else{
                return true
            }
        }
    }


    //统一日志控制
    private fun consoleLog(msg: String, mark: Boolean = true) {
        if (mark) {
            Log.d("SuMing", msg)
        }
    }

}