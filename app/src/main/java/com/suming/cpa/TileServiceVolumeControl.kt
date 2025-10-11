package com.suming.cpa

import android.media.AudioManager
import android.service.quicksettings.TileService

class VolumeTileService : TileService() {

    private var currentVolume = 0


    override fun onClick() {
        super.onClick()
        //音量
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager

        currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, AudioManager.FLAG_SHOW_UI)

    }


}



