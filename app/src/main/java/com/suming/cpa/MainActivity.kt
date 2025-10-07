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
        val buttonT1 = findViewById<Button>(R.id.buttonExit)
        buttonT1.setOnClickListener {
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
        val buttonT2 = findViewById<Button>(R.id.buttonSetting)
        buttonT2.setOnClickListener {
            val intent = Intent(this, Settings::class.java)
            startActivity(intent)
        }
        //按钮：Toolbar指南
        val buttonT3 = findViewById<Button>(R.id.buttonGuidance)
        buttonT3.setOnClickListener {
            val intent = Intent(this, Guidance::class.java)
            startActivity(intent)
        }

        //按钮：深色模式壁纸
        val button6 = findViewById<Button>(R.id.buttonSwitchDark)
        button6.setOnClickListener {
            val intent = Intent(this, DarkMode::class.java)
            startActivity(intent)
        }
        //按钮：通知管理
        val button7=findViewById<Button>(R.id.buttonNotiController)
        button7.setOnClickListener {
            val intent = Intent(this, NotiControl::class.java)
            startActivity(intent)
        }
        //按钮：亮度管理
        val button8=findViewById<Button>(R.id.buttonBrightnessController)
        button8.setOnClickListener {
            val intent = Intent(this, BrightnessController::class.java)
            startActivity(intent)
        }



    } //onCreate END

}//class END