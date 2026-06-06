package com.suming.reparacion.TileServices

import android.content.Intent
import android.content.SharedPreferences
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.content.edit
import com.suming.reparacion.DarkModePure
import com.suming.reparacion.SettingsRequestCenter

class DarkModeTileService : TileService() {

    private var isEnabled = false
    //测试发现全局LocalTile在一些地方不生效,必须单独获取,此处暂不使用
    private var LocalTile = qsTile


    //控制中心长期未打开过,再突然打开,会触发onCreate
    override fun onCreate() {
        super.onCreate()
        consoleLog("磁贴服务：磁贴服务 生命周期 onCreate ")

    }

    override fun onStartListening() {
        super.onStartListening()
        consoleLog("磁贴服务：磁贴服务 生命周期 onStartListening ")
        //更新磁贴状态
        updateTileState()
    }

    @RequiresPermission("android.permission.BROADCAST_CLOSE_SYSTEM_DIALOGS")
    override fun onClick() {
        super.onClick()
        //读取当前磁贴状态(从系统读取,不从内部设置读取)
        val tile = qsTile
        isEnabled = tile.state == Tile.STATE_ACTIVE
        consoleLog("磁贴服务：从系统获取当前磁贴状态: $isEnabled")
        //写入内部设置(取反)
        SettingsRequestCenter.set_State_tile_status_on_dark(this, !isEnabled)

        //更新磁贴状态
        updateTileState(!isEnabled)

        //启动DarkModePure
        Intent(this, DarkModePure::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra("FROM_TILE", true)
            startActivity(this)
        }

    }

    //Functions
    //更新磁贴状态(不知道为什么,必须获取新的qsTile才能正常设置状态)
    private fun updateTileState(targetIsEnabled: Boolean) {
        consoleLog("磁贴服务：变更磁贴状态为: $targetIsEnabled")
        val tile = qsTile
        tile.apply {
            state = if (targetIsEnabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            updateTile()
        }
    }
    //读取当前磁贴状态并刷新
    private fun updateTileState() {
        val tile = qsTile
        isEnabled = SettingsRequestCenter.get_State_tile_status_on_dark(this)
        tile.apply {
            state = if (isEnabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            updateTile()
        }
    }
    //统一日志控制
    private fun consoleLog(msg: String, mark: Boolean = true) {
        if (mark) {
            Log.d("SuMing", msg)
        }
    }

}