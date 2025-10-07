package com.suming.cpa

import android.app.WallpaperManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DarkModePure : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dark_pure)

        if (intent.getBooleanExtra("FROM_TILE", false)) {
            fromTile()
            lifecycleScope.launch {
                delay(1000)
                finish()
            }
        }

        if (intent.getBooleanExtra("FROM_HOME",false)){
            lifecycleScope.launch { delay(500) }
            fromHome()

        }

    }//onCreate END

    //Functions
    private fun setDark() {
        val wallpaperManager = WallpaperManager.getInstance(this)
        val bitmap =
            BitmapFactory.decodeFile("$filesDir/images/selected_image_dark.jpg")
        wallpaperManager.setBitmap(bitmap, null, false,
            WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK)
    }

    private fun setLight() {
        val wallpaperManager = WallpaperManager.getInstance(this)
        val bitmap = BitmapFactory.decodeFile("$filesDir/images/selected_image_light.jpg")
        wallpaperManager.setBitmap(bitmap, null, false,
            WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK)
    }

    private fun fromHome(){
        val sharedPreferences=getSharedPreferences("app_prefs", MODE_PRIVATE)
        if(!sharedPreferences.contains("is_dark_wallpaper_set?") || !sharedPreferences.contains("is_light_wallpaper_set?")){
            notice("您没有设置完全部两张壁纸,请先设置", 11000)
            val switching = findViewById<TextView>(R.id.switching)
            switching.text="无法切换"
            return
        }

        val nightModeFlags = resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
        when (nightModeFlags) {
            android.content.res.Configuration.UI_MODE_NIGHT_YES -> {
                setDark()
            }
            android.content.res.Configuration.UI_MODE_NIGHT_NO -> {
                setLight()
            }
        }
        lifecycleScope.launch {
            delay(1000)
            finish()
        }
    }

    private fun fromTile() {
        var sharedPreferences=getSharedPreferences("app_prefs", MODE_PRIVATE)
        if(!sharedPreferences.contains("is_dark_wallpaper_set?") || !sharedPreferences.contains("is_light_wallpaper_set?")){
            notice("您没有设置完全部两张壁纸,切换失败", 10000)
            val switching = findViewById<TextView>(R.id.switching)
            switching.text="无法切换"
            return
        }

        sharedPreferences = getSharedPreferences("tile_prefs", MODE_PRIVATE)
        val isEnabled = sharedPreferences.getBoolean("isEnabled", true)
        if (isEnabled) {
            setDark()
        } else {
            setLight()
        }
    }

    private var showNoticeJob: Job? = null
    private fun showNoticeJob(text: String, duration: Long) {
        showNoticeJob?.cancel()
        showNoticeJob = lifecycleScope.launch {
            val notice = findViewById<TextView>(R.id.notice)
            val noticeCard = findViewById<CardView>(R.id.noticeCard)
            noticeCard.visibility = View.VISIBLE
            notice.text = text
            delay(duration)
            noticeCard.visibility = View.GONE
        }
    }
    private fun notice(text: String, duration: Long) {
        showNoticeJob(text, duration)
    }

}









