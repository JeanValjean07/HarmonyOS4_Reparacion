package com.suming.cpa

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ApplicationManager: AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //界面设置
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()
        setContentView(R.layout.activity_application_manager)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_application_manager)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }



        mainThreadEntrance()



    }

    override fun onResume() {
        super.onResume()





    }


    private fun mainThreadEntrance(){
        coroutine_read_application.launch {
            //读取应用列表
            val appInfoList = ApplicationEngin(this@ApplicationManager).getApplicationList()

            //如果仅能读到自己,则需要提示打开权限
            if (appInfoList.size == 1 && appInfoList[0].appPackageName == packageName) {

                delay(500)
                showCustomToast("需要打开读取应用列表权限")



                //打开本app的应用信息页面(不启用)
                /*
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.parse("package:$packageName")
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)

                 */

            }

            Log.d("SuMing", "appInfoList: $appInfoList")

        }




    }
    private var coroutine_main: CoroutineScope = CoroutineScope(Dispatchers.Main)
    //读取线程
    private var coroutine_read_application: CoroutineScope = CoroutineScope(Dispatchers.IO)






    }