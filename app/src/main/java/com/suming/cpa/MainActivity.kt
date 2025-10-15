package com.suming.cpa

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Suppress("LocalVariableName")
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        //按钮：Toolbar退出（自杀）
        val ButtonExit = findViewById<Button>(R.id.buttonExit)
        ButtonExit.setOnClickListener {
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_HOME)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            lifecycleScope.launch {
                delay(500)
            }
            val pid = android.os.Process.myPid()
            android.os.Process.killProcess(pid)
        }
        //按钮：Toolbar设置
        val ButtonSetting = findViewById<Button>(R.id.buttonSetting)
        ButtonSetting.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
        //按钮：Toolbar指南
        val ButtonGuidance = findViewById<Button>(R.id.buttonGuidance)
        ButtonGuidance.setOnClickListener {
            val intent = Intent(this, GuidanceActivity::class.java)
            startActivity(intent)
        }

        //按钮：深色模式壁纸
        val ButtonWallpaper = findViewById<Button>(R.id.buttonSwitchDark)
        ButtonWallpaper.setOnClickListener {
            val intent = Intent(this, DarkModeActivity::class.java)
            startActivity(intent)
        }
        //按钮：通知管理
        val ButtonNotiController = findViewById<Button>(R.id.buttonNotiController)
        ButtonNotiController.setOnClickListener {
            val intent = Intent(this, NotiControl::class.java)
            startActivity(intent)
        }
        //按钮：桌面时钟


        //按钮：音量控制
        val ButtonVolumeController = findViewById<Button>(R.id.buttonVolumeController)
        ButtonVolumeController.setOnClickListener {
            val intent = Intent(this, VolumeControl::class.java)
            startActivity(intent)
        }





        //不显示的按钮
        //按钮：后台管理
        val ButtonBackgroundCheck = findViewById<Button>(R.id.buttonBrightnessController)
        ButtonBackgroundCheck.visibility = Button.GONE
        ButtonBackgroundCheck.setOnClickListener {
            val intent = Intent(this, BackgroundCheckActivity::class.java)
            startActivity(intent)
        }





    } //onCreate END

}//class END