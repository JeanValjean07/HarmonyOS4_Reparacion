package com.suming.reparacion.ActivityComponents.NotificationManager

import com.suming.reparacion.DataPack.Connect
import com.suming.reparacion.DataPack.NotificationPack
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object NotificationManagerRepo {


    private val _list = MutableStateFlow(listOf<NotificationPack>())
    val list: StateFlow<List<NotificationPack>> = _list.asStateFlow()
    //添加通知
    fun add(notificationPack: NotificationPack) {
        val currentList = _list.value.toMutableList()
        //只判断包名,标题,内容是否相同
        val existingIndex = currentList.indexOfFirst {
            it.packageName == notificationPack.packageName &&
                    it.title == notificationPack.title &&
                    it.text == notificationPack.text
        }

        if (existingIndex >= 0) {
            //已存在时：覆盖成最新
            currentList[existingIndex] = notificationPack
            //或把旧的移到顶部,丢掉新的
            //val notification = currentList.removeAt(existingIndex)
            //currentList.add(0, notification)
        } else {
            //不存在时：直接添加到列表顶部
            currentList.add(0, notificationPack)
        }

        _list.value = currentList
    }
    //获取通知数量
    fun getCount() = _list.value.size
    //更新通知数量
    fun updateCount(){
        setNotificationCount(getCount())
    }
    //清除缓存列表内所有通知
    fun clearAll(){
        _list.value = listOf()
        updateCount()
    }


    //取消一条通知
    private var needCancelKey = ""
    fun getNeedCancelKey(): String{
        val tempKey = needCancelKey
        needCancelKey = ""
        return tempKey
    }
    fun setNeedCancelKey(key: String){
        needCancelKey = key
        setServiceConnect(Connect.service_intent_cancel)
    }
    //隐藏一条通知(极端延长来实现)
    private var needSnoozeKey = ""
    fun getNeedSnoozeKey(): String{
        val tempKey = needSnoozeKey
        needSnoozeKey = ""
        return tempKey
    }
    fun setNeedSnoozeKey(key: String){
        needSnoozeKey = key
        setServiceConnect(Connect.service_intent_snooze)
    }
    //延后一条通知
    private var needDelayKey = ""
    private var delaySeconds = 0
    fun getNeedDelayKey(): Pair<String,Int>{
        val tempKey = needDelayKey
        val tempDelaySeconds = delaySeconds
        needDelayKey = ""
        delaySeconds = 0
        return Pair(tempKey,tempDelaySeconds)
    }
    fun setNeedDelayKey(key: String,seconds: Int){
        needDelayKey = key
        delaySeconds = seconds
        setServiceConnect(Connect.service_intent_delay_seconds)
    }



    //服务观察状态
    private val _service_connect = MutableStateFlow("")
    val service_connect: StateFlow<String> = _service_connect.asStateFlow()
    fun setServiceConnect(intent: String){
        _service_connect.value = intent
    }
    fun clearServiceConnect(){
        setServiceConnect("")
    }
    //通知访问权限
    private var state_isPermissionGranted = false
    fun setIsPermissionGranted(granted: Boolean) {
        state_isPermissionGranted = granted
        updateErrorCoverMark()
    }
    //当前通知数量
    private var notificationCount = 0
    fun setNotificationCount(count: Int) {
        notificationCount = count
        updateErrorCoverMark()
    }
    //当前服务状态
    private var state_isServiceOnline = false
    fun setIsServiceOnline(online: Boolean) {
        state_isServiceOnline = online
        updateErrorCoverMark()
    }
    //ErrorCover可观察标记
    private val _showErrorCoverMark = MutableStateFlow("")
    val showErrorCoverMark: StateFlow<String> = _showErrorCoverMark.asStateFlow()
    //根据多种状态融合出最终标记
    private const val mark_all_right = "NOTIFICATION_ALL_RIGHT"
    private const val mark_zero_notice_here = "NOTIFICATION_ERROR_ZERO_NOTICE_HERE"
    private const val mark_service_offline = "NOTIFICATION_ERROR_SERVICE_OFFLINE"
    private const val mark_permission = "NOTIFICATION_ERROR_PERMISSION"
    fun updateErrorCoverMark() {
        when (state_isPermissionGranted) {
            //已开启权限
            true -> {
                if (state_isServiceOnline) {
                    if (notificationCount == 0) {
                        finalSetErrorCoverMark(mark_zero_notice_here)
                    }else{
                        finalSetErrorCoverMark(mark_all_right)
                    }
                }else{
                    finalSetErrorCoverMark(mark_service_offline)
                }
            }
            //未开启权限
            false -> {
                finalSetErrorCoverMark(mark_permission)
            }
        }
    }
    private fun finalSetErrorCoverMark(mark: String) {
        _showErrorCoverMark.value = mark
    }



}