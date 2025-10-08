package com.suming.cpa

import android.app.WallpaperManager
import android.content.Intent

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
            startFromControlCenter()
        }

        if (intent.getBooleanExtra("FROM_HOME",false)){
            lifecycleScope.launch {
                delay(500)
                startFromDesktopLauncher()
            }
        }

        //错误提示:点击跳转至壁纸设置页
        val noticeCard = findViewById<CardView>(R.id.noticeCard)
        noticeCard.setOnClickListener {
            val intent = Intent(this, DarkModeActivity::class.java)
            startActivity(intent)
        }


    }//onCreate END

    //Functions
    private fun setDarkWallpaper() {
        val wallpaperManager = WallpaperManager.getInstance(this)
        val bitmap =
            BitmapFactory.decodeFile("$filesDir/images/selected_image_dark.jpg")
        wallpaperManager.setBitmap(bitmap, null, false,
            WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK)
    }

    private fun setLightWallpaper() {
        val wallpaperManager = WallpaperManager.getInstance(this)
        val bitmap = BitmapFactory.decodeFile("$filesDir/images/selected_image_light.jpg")
        wallpaperManager.setBitmap(bitmap, null, false,
            WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK)
    }

    private fun startFromDesktopLauncher(){
        val sharedPreferences=getSharedPreferences("app_prefs", MODE_PRIVATE)
        if(!sharedPreferences.contains("is_dark_wallpaper_set?") || !sharedPreferences.contains("is_light_wallpaper_set?")){
            notice("您没有设置完全部两张壁纸,请先设置")
            val switching = findViewById<TextView>(R.id.switching)
            switching.text="无法切换"
            return
        }

        val nightModeFlags = resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
        when (nightModeFlags) {
            android.content.res.Configuration.UI_MODE_NIGHT_YES -> {
                setDarkWallpaper()
            }
            android.content.res.Configuration.UI_MODE_NIGHT_NO -> {
                setLightWallpaper()
            }
        }
        lifecycleScope.launch {
            delay(1000)
            finish()
        }
    }

    private fun startFromControlCenter() {
        var sharedPreferences=getSharedPreferences("app_prefs", MODE_PRIVATE)
        if(!sharedPreferences.contains("is_dark_wallpaper_set?") || !sharedPreferences.contains("is_light_wallpaper_set?")){
            notice("您没有设置完全部两张壁纸,切换失败")
            val switching = findViewById<TextView>(R.id.switching)
            switching.text="无法切换"
            return
        }

        sharedPreferences = getSharedPreferences("tile_prefs", MODE_PRIVATE)
        val isEnabled = sharedPreferences.getBoolean("isEnabled", true)
        if (isEnabled) { setDarkWallpaper() } else { setLightWallpaper() }

        lifecycleScope.launch {
            delay(1000)
            finish()
        }
    }


    private var showNoticeJob: Job? = null
    private fun showNoticeJob(text: String) {
        showNoticeJob?.cancel()
        showNoticeJob = lifecycleScope.launch {
            val notice = findViewById<TextView>(R.id.notice)
            val noticeCard = findViewById<CardView>(R.id.noticeCard)
            noticeCard.visibility = View.VISIBLE
            notice.text = text
        }
    }
    private fun notice(text: String) {
        showNoticeJob(text)
    }

}









