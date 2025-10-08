package com.suming.cpa

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Suppress("LocalVariableName")
class BackgroundCheck : AppCompatActivity() {


    @SuppressLint("ServiceCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()
        setContentView(R.layout.activity_background_check)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_background_check)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }



        //退出按钮
        val ButtonExit = findViewById<ImageButton>(R.id.ButtonExit)
        ButtonExit.setOnClickListener {
            finish()
        }

        //开始执行轮流打开按钮
        val ButtonLooperStart = findViewById<CardView>(R.id.ButtonLooperStart)
        ButtonLooperStart.setOnClickListener {

            lifecycleScope.launch(Dispatchers.IO) {
                spyAppLabel("高德地图")

                spyAppLabel("淘宝")

                spyAppLabel("闲鱼")


            }

        }












    } //onCreate


    @SuppressLint("QueryPermissionsNeeded")
    private fun spyAppLabel(keyword: String): Boolean? {
        val pm = packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER)

        val activities = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.queryIntentActivities(mainIntent, PackageManager.ResolveInfoFlags.of(0L))
        } else {
            pm.queryIntentActivities(mainIntent, 0)
        }


        for (ri in activities) {
            val label = ri.loadLabel(pm).toString()
            if (label.contains(keyword, true)) {
                val packageName = ri.activityInfo.packageName
                looperStart(packageName)
                return true
            }
        }
        return false
    }

    private fun looperStart(packageName: String){
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)


    }
}