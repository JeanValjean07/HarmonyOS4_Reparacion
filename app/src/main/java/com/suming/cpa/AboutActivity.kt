package com.suming.cpa

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat

@Suppress("LocalVariableName")
class AboutActivity : AppCompatActivity() {
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
        val ButtonExit = findViewById<ImageButton>(R.id.buttonExit)
        ButtonExit.setOnClickListener {
            finish()
        }
        //按钮：查看Github仓库
        val ButtonGoGithub = findViewById<TextView>(R.id.buttonGoGithubRelease)
        ButtonGoGithub.setOnClickListener {
            val url = "https://github.com/JeanValjean07/HarmonyOS4_Reparacion/releases"
            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
            startActivity(intent)
        }
        //按钮：反馈问题
        val ButtonReportIssue = findViewById<TextView>(R.id.buttonReportIssue)
        ButtonReportIssue.setOnClickListener {
            val url = "https://space.bilibili.com/1206378184"
            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
            startActivity(intent)
        }
        //超链接：开放源代码许可
        val ButtonOpenSourceLicense = findViewById<TextView>(R.id.openSourceLicense)
        ButtonOpenSourceLicense.paint.isUnderlineText = true
        ButtonOpenSourceLicense.setOnClickListener {
            startActivity(
                Intent(this, com.google.android.gms.oss.licenses.OssLicensesMenuActivity::class.java)
            )
        }



    }//onCreate END
}//class END