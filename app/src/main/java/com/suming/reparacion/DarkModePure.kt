package com.suming.reparacion

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.suming.reparacion.AddonTools.showCustomToast
import com.suming.reparacion.FunctionalPack.BitmapLoader
import com.suming.reparacion.FunctionalPack.WallpaperSetor
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DarkModePure : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //界面配置
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dark_pure)

        //区分点击意图
        if (intent.getBooleanExtra("FROM_TILE", false)) {
            startFromControlCenter()
        } else if (intent.getBooleanExtra("FROM_HOME",false)){
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


    }

    //Entrance Functions
    //从桌面快捷方式启动
    private fun startFromDesktopLauncher(){
        //检查是否设置完了全部壁纸
        if(checkWallpaperSet()){
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

        }else{
            notice("您没有设置完全部两张壁纸,请先设置")
            val switching = findViewById<TextView>(R.id.switching)
            switching.text="无法切换"
        }

    }
    //从控制中心磁贴启动
    private fun startFromControlCenter() {
        //检查是否设置完了全部壁纸
        if(checkWallpaperSet()){

            //读取当前磁贴状态(在服务中修改,此处仅读取)
            val isEnabled = SettingsRequestCenter.get_State_tile_status_on_dark(this)
            if (isEnabled) { setDarkWallpaper() } else { setLightWallpaper() }

            //自动退出
            lifecycleScope.launch {
                delay(1000)
                finish()
            }
        }else{
            notice("您没有设置完全部两张壁纸,切换失败")
            val switching = findViewById<TextView>(R.id.switching)
            switching.text="无法切换"
        }
    }


    //Functions
    //设置深色壁纸
    private fun setDarkWallpaper() {
        val wallpaperSetor = WallpaperSetor()
        val needClipped = SettingsRequestCenter.get_PREFS_SlightMove_Clip(this)
        val bitmap = BitmapLoader().loadBitmap(this,"dark",needClipped)
        if (bitmap != null){
            wallpaperSetor.applySystemWallpaper(bitmap, this)
        }else{
            showCustomToast("取图时发生错误")
        }
    }
    //设置浅色壁纸
    private fun setLightWallpaper() {
        val wallpaperSetor = WallpaperSetor()
        val needClipped = SettingsRequestCenter.get_PREFS_SlightMove_Clip(this)
        val bitmap = BitmapLoader().loadBitmap(this,"light",needClipped)
        if (bitmap != null){
            wallpaperSetor.applySystemWallpaper(bitmap, this)
        }else{
            showCustomToast("取图时发生错误")
        }
    }
    //检查是否已设置全部两张壁纸
    private fun checkWallpaperSet(): Boolean {
        val isDarkWallpaperSet = SettingsRequestCenter.get_State_dark_paper_set(this)
        val isLightWallpaperSet = SettingsRequestCenter.get_State_light_paper_set(this)

        return isDarkWallpaperSet && isLightWallpaperSet
    }


    //手搓提示
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









