package com.suming.cpa

import android.annotation.SuppressLint
import android.os.Bundle
import android.provider.Settings
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat

class BrightnessController : AppCompatActivity() {
    @SuppressLint("ServiceCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()
        setContentView(R.layout.activity_brightness)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_brightness)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }



        val windowInfo = window.attributes
        var initBrightness = windowInfo.screenBrightness


        if (initBrightness < 0) {
            initBrightness = Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS) / 255f
        }



        val currentBrightness = findViewById<TextView>(R.id.currentBrightness)
        currentBrightness.text = "${initBrightness}"








    }
}