package com.suming.cpa

import android.content.Intent
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat

@Suppress("LocalVariableName")
class GuidanceActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()
        setContentView(R.layout.activity_guidance)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_guidance)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        //返回按钮
        val ButtonExit = findViewById<ImageButton>(R.id.buttonExit)
        ButtonExit.setOnClickListener {
            finish()
        }
        //按钮：反馈问题
        val ButtonReportIssue = findViewById<TextView>(R.id.buttonReportIssue)
        ButtonReportIssue.setOnClickListener {
            val url = "https://space.bilibili.com/1206378184"
            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
            startActivity(intent)
        }
        //SvgRepo
        val buttonGoSvgRepo = findViewById<FrameLayout>(R.id.buttonGoSvgRepo)
        buttonGoSvgRepo.setOnClickListener {
            val url = "https://www.svgrepo.com/"
            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
            startActivity(intent)
        }




    }//onCreate END


}//class END
