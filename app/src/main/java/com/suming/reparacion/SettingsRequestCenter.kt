package com.suming.reparacion

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

@Suppress("unused")
object SettingsRequestCenter {

    //设置清单-深色模式壁纸---------------------------------------------------
    private lateinit var PREFS_DarkMode: SharedPreferences
    private var state_PREFS_DarkMode_initialized = false

    //设置项：微动数值
    private var PREFS_SlightMove_value = -1
    fun set_PREFS_SlightMove_value(context: Context, value:Int){
        if (!state_PREFS_DarkMode_initialized) {
            PREFS_DarkMode = context.getSharedPreferences("PREFS_DarkMode", 0)
        }
        PREFS_SlightMove_value = value
        PREFS_DarkMode.edit { putInt("PREFS_SlightMove_value", value) }
    }
    fun get_PREFS_SlightMove_value(context: Context): Int {
        //确保配置清单已初始化
        if (!state_PREFS_DarkMode_initialized) {
            PREFS_DarkMode = context.getSharedPreferences("PREFS_DarkMode", 0)
            state_PREFS_DarkMode_initialized = true
        }
        //确保配置项已被读取过
        if (PREFS_SlightMove_value == -1) {
            PREFS_SlightMove_value = PREFS_DarkMode.getInt("PREFS_SlightMove_value", -1)
            if (PREFS_SlightMove_value == -1) {
                PREFS_DarkMode.edit { putInt("PREFS_SlightMove_value", 0) }
            }
        }
        return PREFS_SlightMove_value
    }
    //设置项：启用微动效果裁剪
    private var PREFS_SlightMove_Clip = -1
    fun set_PREFS_SlightMove_Clip(EnableSlightMove: Boolean){
        PREFS_SlightMove_Clip = if (EnableSlightMove) 1 else 0
        PREFS_DarkMode.edit { putInt("PREFS_SlightMove_Clip", if (EnableSlightMove) 1 else 0) }
    }
    fun get_PREFS_SlightMove_Clip(context: Context): Boolean{
        //确保配置清单已初始化
        if (!state_PREFS_DarkMode_initialized){
            PREFS_DarkMode = context.getSharedPreferences("PREFS_DarkMode", 0)
            state_PREFS_DarkMode_initialized = true
        }
        //确保配置项已被读取过
        if (PREFS_SlightMove_Clip == -1){
            PREFS_SlightMove_Clip = PREFS_DarkMode.getInt("PREFS_SlightMove_Clip", -1)
            if (PREFS_SlightMove_Clip == -1){
                PREFS_DarkMode.edit { putInt("PREFS_SlightMove_Clip", 0) }
            }
        }

        return PREFS_SlightMove_Clip == 1
    }
    //设置项：将裁剪后的图片保存到外部相册
    private var PREFS_Save_Clip_Out = -1
    fun set_PREFS_Save_Clip_Out(EnableSaveClipOut: Boolean){
        PREFS_Save_Clip_Out = if (EnableSaveClipOut) 1 else 0
        PREFS_DarkMode.edit { putInt("PREFS_Save_Clip_Out", if (EnableSaveClipOut) 1 else 0) }
    }
    fun get_PREFS_Save_Clip_Out(context: Context): Boolean{
        //确保配置清单已初始化
        if (!state_PREFS_DarkMode_initialized){
            PREFS_DarkMode = context.getSharedPreferences("PREFS_DarkMode", 0)
            state_PREFS_DarkMode_initialized = true
        }
        //确保配置项已被读取过
        if (PREFS_Save_Clip_Out == -1){
            PREFS_Save_Clip_Out = PREFS_DarkMode.getInt("PREFS_Save_Clip_Out", -1)
            if (PREFS_Save_Clip_Out == -1){
                PREFS_DarkMode.edit { putInt("PREFS_Save_Clip_Out", 0) }
            }
        }

        return PREFS_Save_Clip_Out == 1
    }
    //状态值：是否已设置浅色壁纸
    private var STATE_dark_paper_set = -1
    fun set_State_dark_paper_set(context: Context,isSet: Boolean){
        //确保已初始化表
        if(!state_PREFS_DarkMode_initialized){
            PREFS_DarkMode = context.getSharedPreferences("PREFS_DarkMode", 0)
            state_PREFS_DarkMode_initialized = true
        }

        STATE_dark_paper_set = if (isSet) 1 else 0
        PREFS_DarkMode.edit { putInt("STATE_dark_paper_set", STATE_dark_paper_set) }
    }
    fun get_State_dark_paper_set(context: Context): Boolean {
        //确保已初始化表
        if(!state_PREFS_DarkMode_initialized){
            PREFS_DarkMode = context.getSharedPreferences("PREFS_DarkMode", 0)
            state_PREFS_DarkMode_initialized = true
        }
        //确保配置项已被读取过
        if (STATE_dark_paper_set == -1){
            STATE_dark_paper_set = PREFS_DarkMode.getInt("STATE_dark_paper_set", -1)
            if (STATE_dark_paper_set == -1){
                PREFS_DarkMode.edit { putInt("STATE_dark_paper_set", 0) }
            }
        }

        return STATE_dark_paper_set == 1
    }
    //状态值：是否已设置深色壁纸
    private var STATE_light_paper_set = -1
    fun set_State_light_paper_set(context: Context,isSet: Boolean){
        //确保已初始化表
        if(!state_PREFS_DarkMode_initialized){
            PREFS_DarkMode = context.getSharedPreferences("PREFS_DarkMode", 0)
            state_PREFS_DarkMode_initialized = true
        }

        STATE_light_paper_set = if (isSet) 1 else 0
        PREFS_DarkMode.edit { putInt("STATE_light_paper_set", STATE_light_paper_set) }
    }
    fun get_State_light_paper_set(context: Context): Boolean {
        //确保已初始化表
        if(!state_PREFS_DarkMode_initialized){
            PREFS_DarkMode = context.getSharedPreferences("PREFS_DarkMode", 0)
            state_PREFS_DarkMode_initialized = true
        }
        //确保配置项已被读取过
        if (STATE_light_paper_set == -1){
            STATE_light_paper_set = PREFS_DarkMode.getInt("STATE_light_paper_set", -1)
            if (STATE_light_paper_set == -1){
                PREFS_DarkMode.edit { putInt("STATE_light_paper_set", 0) }
            }
        }

        return STATE_light_paper_set == 1
    }
    //状态值：磁贴当前状态
    private var STATE_tile_status_on_dark = -1
    fun set_State_tile_status_on_dark(context: Context,isDark: Boolean) {
        //确保已初始化表
        if (!state_PREFS_DarkMode_initialized) {
            PREFS_DarkMode = context.getSharedPreferences("PREFS_DarkMode", 0)
            state_PREFS_DarkMode_initialized = true
        }

        STATE_tile_status_on_dark = if (isDark) 1 else 0

        PREFS_DarkMode.edit { putInt("STATE_tile_status_on_dark", STATE_tile_status_on_dark) }
    }
    fun get_State_tile_status_on_dark(context: Context): Boolean {
        //确保已初始化表
        if (!state_PREFS_DarkMode_initialized) {
            PREFS_DarkMode = context.getSharedPreferences("PREFS_DarkMode", 0)
            state_PREFS_DarkMode_initialized = true
        }
        //确保配置项已被读取过
        if (STATE_tile_status_on_dark == -1){
            STATE_tile_status_on_dark = PREFS_DarkMode.getInt("STATE_tile_status_on_dark", -1)
            if (STATE_tile_status_on_dark == -1){
                PREFS_DarkMode.edit { putInt("STATE_tile_status_on_dark", 0) }
            }
        }
        return STATE_tile_status_on_dark == 1
    }



    //设置清单-通知管理器---------------------------------------------------
    private var PREFS_Notification: SharedPreferences? = null

    //设置项：保留内容为空的通知
    private var PREFS_Notification_Keep_Empty = -1
    fun set_PREFS_Notification_Keep_Empty(context: Context,isKeep: Boolean){
        if(PREFS_Notification == null){
            PREFS_Notification = context.getSharedPreferences("PREFS_Notification", 0)
        }

        PREFS_Notification_Keep_Empty = if (isKeep) 1 else 0
        PREFS_Notification?.edit { putInt("PREFS_Notification_Keep_Empty", PREFS_Notification_Keep_Empty) }
    }
    fun get_PREFS_Notification_Keep_Empty(context: Context): Boolean {
        if(PREFS_Notification == null){
            PREFS_Notification = context.getSharedPreferences("PREFS_Notification", 0)
        }
        PREFS_Notification_Keep_Empty = PREFS_Notification?.getInt("PREFS_Notification_Keep_Empty", -1) ?: -1
        if (PREFS_Notification_Keep_Empty == -1){
            PREFS_Notification?.edit { putInt("PREFS_Notification_Keep_Empty", 0) }
        }

        return PREFS_Notification_Keep_Empty == 1
    }
















}//</object>
