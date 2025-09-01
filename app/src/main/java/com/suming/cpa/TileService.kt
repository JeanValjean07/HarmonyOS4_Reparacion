
package com.suming.cpa

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.Icon
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresPermission
import androidx.core.content.edit


class DarkModeTileService : TileService() {
    private lateinit var prefs: SharedPreferences
    private var isEnabled = false

    override fun onCreate() {
        super.onCreate()

        prefs = getSharedPreferences("tile_prefs", MODE_PRIVATE)
        isEnabled = prefs.getBoolean("is_enabled", false)
    }

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }


    @RequiresPermission("android.permission.BROADCAST_CLOSE_SYSTEM_DIALOGS")
    override fun onClick() {
        super.onClick()

        val sharedPreferences=getSharedPreferences("tile_prefs", MODE_PRIVATE)
        isEnabled=sharedPreferences.getBoolean("isEnabled",true)
        if (isEnabled == true) {
            isEnabled = false
        } else {
            isEnabled = true
        }
        sharedPreferences.edit {
            putBoolean("isEnabled", isEnabled)
            apply()
        }

        updateTileState()


        val intent = Intent(this, DarkModePure::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra("FROM_TILE", true)
            startActivity(this)
        }

    }



    private fun updateTileState() {
        val tile = qsTile
        tile?.apply {
            state = if (isEnabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            icon = Icon.createWithResource(this@DarkModeTileService,
                R.drawable.ic_tile_selector)
            updateTile()
        }
    }






}