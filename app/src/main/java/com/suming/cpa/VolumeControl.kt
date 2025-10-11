package com.suming.cpa

import android.media.AudioManager
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat

@Suppress("LocalVariableName")
class VolumeControl: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()
        setContentView(R.layout.activity_volume_control)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_volume_control)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        //按钮：返回
        val ButtonExit = findViewById<ImageButton>(R.id.buttonExit)
        ButtonExit.setOnClickListener {
            finish()
        }
        //点击彩蛋
        val SignVolume = findViewById<ImageView>(R.id.sign_volume)
        SignVolume.setOnClickListener {
            val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
            val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, AudioManager.FLAG_SHOW_UI)
        }






    }


}