package com.suming.cpa

import android.app.Service
import android.content.Intent
import android.media.AudioManager
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class VolumeControlService : Service() {

    private val audioManager by lazy { getSystemService(AUDIO_SERVICE) as AudioManager }
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(1, notification())
        //watchSessions()
        return START_STICKY
    }


/*
    private fun watchSessions() {
        scope.launch {
            while (isActive) {
                val sessions = audioManager.activePlaybackConfigurations
                    .mapNotNull { it.audioSessionId }
                    .distinct()

                sessions.forEach { sessionId ->
                    val app = application as EqApplication
                    if (sessionId != 0 && !app.eqCache.containsKey(sessionId)) {
                        createEq(sessionId)?.let { app.eqCache[sessionId] = it }
                    }
                }
                delay(1000)
            }
        }
    }

    private fun createEq(sessionId: Int): android.media.audiofx.Equalizer? =
        android.media.audiofx.Equalizer(0, sessionId).apply {
            enabled = true
            // 默认平坦，也可读取本地预设
            (0 until numberOfBands).forEach { setBandLevel(it, 0) }
        }

 */



    //构建通知
    private fun notification() = NotificationCompat.Builder(this, "eq")
        .setSmallIcon(R.drawable.ic_tile_volume)
        .setContentTitle("音量均衡器服务运行中")
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .build()

    override fun onBind(intent: Intent?) = null
}