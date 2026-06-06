package com.suming.reparacion.TileServices

import android.media.AudioManager
import android.service.quicksettings.TileService

class VolumeTileService : TileService() {

    private var currentVolume = 0
    private var audioManager: AudioManager? = null

    override fun onClick() {
        super.onClick()

        //音量弹出音量面板
        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager

        currentVolume = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 0

        //FLAG_SHOW_UI控制面板显示
        audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, AudioManager.FLAG_SHOW_UI)

    }


}



