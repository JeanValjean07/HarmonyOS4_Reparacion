package com.suming.cpa

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat

class About : AppCompatActivity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()
        setContentView(R.layout.activity_about)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_about)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        //显示版本
        val version = packageManager.getPackageInfo(packageName, 0).versionName
        val versionText = findViewById<TextView>(R.id.version)
        versionText.text = "版本: $version"


        //按钮：返回
        val buttonT1 = findViewById<ImageButton>(R.id.buttonExit)
        buttonT1.setOnClickListener {
            finish()
        }
        //按钮：查看开源许可
        val buttonLicense = findViewById<Button>(R.id.buttonLicense)
        buttonLicense.setOnClickListener {
            startActivity(
                Intent(this, com.google.android.gms.oss.licenses.OssLicensesMenuActivity::class.java)
            )
        }
        //按钮：查看Github仓库
        val button2 = findViewById<Button>(R.id.buttonGithub)
        button2.setOnClickListener {
            val url = "https://github.com/JeanValjean07/H-api31-complement-A"
            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
            startActivity(intent)
        }
        //按钮：查看酷安主页
        val button3 = findViewById<Button>(R.id.buttonCoolApk)
        button3.setOnClickListener {
            val url = "http://www.coolapk.com/u/3105725"
            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
            startActivity(intent)
        }


    }//onCreate END
}//class END