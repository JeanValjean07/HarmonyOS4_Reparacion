package com.suming.cpa

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.suming.cpa.NotiService.Companion.ACTION_NOTIFICATION_RECEIVED
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


interface ReceiverCallback {
    fun onNotificationReceived(packageName:String, title:String, content: String)
}

@Suppress("LocalVariableName")
class NotiControl: AppCompatActivity(), ReceiverCallback {

    private val NotiList = mutableStateListOf<MutableList<String>>()
    private var count=0
    private lateinit var receiver: Receiver

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()
        setContentView(R.layout.activity_noticontrol)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_noticontrol)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //准备工作
        preCheck()
        //调用showContent先展示出默认内容
        showContent()

        //注册接收器
        receiver = object : Receiver() {
            override fun onReceive(context: Context, intent: Intent) {
                intent.let {
                    if (it.action == ACTION_NOTIFICATION_RECEIVED) {
                        val packageName = it.getStringExtra(NotiService.EXTRA_PACKAGE_NAME) ?: ""
                        val title = it.getStringExtra(NotiService.EXTRA_TITLE) ?: ""
                        val content = it.getStringExtra(NotiService.EXTRA_CONTENT) ?: ""
                        onNotificationReceived(packageName, title, content)
                    }
                }
            }
        }
        val filter = IntentFilter(ACTION_NOTIFICATION_RECEIVED).apply {
            addCategory(Intent.CATEGORY_DEFAULT)
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter)


        //按钮：返回上级
        val ButtonExit = findViewById<ImageButton>(R.id.buttonToolbarExit)
        ButtonExit.setOnClickListener {
            finish()
        }
        //按钮：立即刷新
        val ButtonRefresh = findViewById<Button>(R.id.buttonRead)
        ButtonRefresh.setOnClickListener {
            val enabledListenerPackages = NotificationManagerCompat.getEnabledListenerPackages(this)
            val isNotificationListenerEnabled = enabledListenerPackages.contains(packageName)
            if (!isNotificationListenerEnabled) {
                notice("通知监听权限未开启", 5000)
                return@setOnClickListener
            } else{
                val areNotificationsEnabled = NotificationManagerCompat.from(this).areNotificationsEnabled()
                if (areNotificationsEnabled) {
                    NotiList.clear()
                    count=0
                    sendNotice()
                } else {
                    notice("需要发送通知来触发监听器的回调函数,请允许本APP通知权限", 8000)
                    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    intent.putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                    startActivity(intent)
                }
            }
        }
        //按钮：清空列表
        val ButtonClear = findViewById<Button>(R.id.buttonClear)
        ButtonClear.setOnClickListener {
            NotiList.clear()
            count=0
        }
        //Notice卡片点击时关闭
        val NoticeCard = findViewById<CardView>(R.id.noticeCard)
        NoticeCard.setOnClickListener {
            NoticeCard.visibility = View.GONE
        }


        //按钮：开启通知访问权限
        val ButtonOpenNotiListener = findViewById<Button>(R.id.buttonLetsOpenIt)
        ButtonOpenNotiListener.setOnClickListener {
            goNotiSetting()
        }
        //按钮：搜索应用关闭
        val ButtonAppList = findViewById<Button>(R.id.buttonAppList)
        ButtonAppList.setOnClickListener {
            notice("该功能完善中", 5000)
            return@setOnClickListener
        }

    }//onCreate END

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onResume() {
        super.onResume()
        preCheck()
    }

    //Functions
    override fun onNotificationReceived(packageName: String, title: String, content: String) {
        moveContent(packageName,title, content)

    }

    private fun moveContent(packageName: String, title: String, content: String) {

        fun addRow(NotiList: MutableList<MutableList<String>>,row: List<String>) {
            val newRow = mutableListOf<String>().apply {
                add(count.toString())
                addAll(row)
            }
            NotiList.add(newRow)
        }

        addRow(NotiList,listOf((count).toString(), packageName, title, content))

        count++

        showContent()

    }

    @Composable
    fun ClickableItem(row: List<String>){
        val context = LocalContext.current


        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(7.dp)
                .clickable {
                    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                        .putExtra(Settings.EXTRA_APP_PACKAGE, row[2])
                    context.startActivity(intent)
                }
        ){
            var isVisible by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                isVisible = true
            }
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(animationSpec = tween(durationMillis = 300))
            ) {
            Text(
                text = "${row[1]} ${row[3]}\n${row[4]}",
                style = TextStyle(fontSize = 14.sp),
                color = colorResource(id = R.color.HeadText),
            )
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth(),
                thickness = 1.dp,
                color = colorResource(id = R.color.HeadText)
            )
            }
        }
    }

    private fun showContent() {
        val composeNotice = findViewById<ComposeView>(R.id.composeNotices)
        composeNotice.setContent {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
                    .heightIn(max = 50000.dp, min = 160.dp)
            ) {

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 50000.dp, min = 160.dp)
                        .padding(13.dp)
                        .background(colorResource(id = R.color.HeadBackground))
                        .border(
                            2.dp,
                            colorResource(id = R.color.HeadText),
                            RoundedCornerShape(20.dp)
                        )
                )
                {
                    if (NotiList.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 100.dp)
                                    .padding(top = 30.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                var isVisible by remember { mutableStateOf(false) }
                                LaunchedEffect(Unit) {
                                    isVisible = true
                                }
                                AnimatedVisibility(
                                    visible = isVisible,
                                    enter = fadeIn(animationSpec = tween(durationMillis = 300))
                                ) {
                                    Text(
                                        text = "暂无通知内容",
                                        style = TextStyle(fontSize = 14.sp),
                                        color = colorResource(id = R.color.HeadText)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(36.dp))

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 100.dp)
                                    .padding(bottom = 30.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                var isVisible by remember { mutableStateOf(false) }
                                LaunchedEffect(Unit) {
                                    isVisible = true
                                }
                                AnimatedVisibility(
                                    visible = isVisible,
                                    enter = fadeIn(animationSpec = tween(durationMillis = 300))
                                ) {
                                    Text(
                                        text = "请开启通知访问权限或点击顶部立即刷新",
                                        style = TextStyle(fontSize = 14.sp),
                                        color = colorResource(id = R.color.HeadText)
                                    )
                                }
                            }
                        }
                    } else {
                        items(NotiList) { row ->
                            ClickableItem(row)
                        }
                    }
                }
            }
        }
    }

    private fun goNotiSetting(){
        val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun sendNotice(){
        val channelId = "refresh"
        val notificationId = 114
        val notificationTitle = "刷新通知列表"
        val notificationContent = "此条通知将自动删除"

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(notificationTitle)
            .setContentText(notificationContent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val notificationManager: NotificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notificationBuilder.build())

        Handler(Looper.getMainLooper()).postDelayed({
            notificationManager.cancel(notificationId)
        }, 200)
    }

    private fun preCheck(){
        //检查权限是否开启
        val enabledListenerPackages = NotificationManagerCompat.getEnabledListenerPackages(this)
        val isNotificationListenerEnabled = enabledListenerPackages.contains(packageName)
        if(isNotificationListenerEnabled){

            val textA2 = findViewById<TextView>(R.id.textA2)
            textA2.text = "已开启通知访问权限,使用后建议关闭"

            val button1 = findViewById<Button>(R.id.buttonLetsOpenIt)
            button1.text = "去关闭"
        } else{
            val textA2 = findViewById<TextView>(R.id.textA2)
            textA2.text = "开启通知访问权限以读取通知信息"

            val button1 = findViewById<Button>(R.id.buttonLetsOpenIt)
            button1.text = "去开启"
        }
        //申请通知权限
        val channelId = "refresh"
        val channelName = "通知监听器刷新"
        val channelDescription = "用于刷新通知监听器状态"
        val importance = NotificationManager.IMPORTANCE_HIGH

        val channel = NotificationChannel(channelId, channelName, importance).apply {  //固有
            description = channelDescription
        }
        val notificationManager: NotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
        //读取设置
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val showGuidance = sharedPreferences.getInt("showGuidance", 0)
        val showGuidanceAnimation = sharedPreferences.getInt("showGuidanceAnimation", 0)
        //活动功能介绍
        if (showGuidance == 1){
            val composeDescription = findViewById<ComposeView>(R.id.composeDescription)
            composeDescription.setContent {
                var Y =300
                var T = 300
                if (showGuidanceAnimation==0){
                    Y =0
                    T = 0
                }
                var isVisible by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    isVisible = true
                }
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(animationSpec = tween(durationMillis = 100)) + slideInVertically(
                        initialOffsetY = { Y },
                        animationSpec = tween(durationMillis = T)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(13.dp)
                            .background(colorResource(id = R.color.HeadBackground))
                            .border(
                                2.dp,
                                colorResource(id = R.color.HeadText),
                                RoundedCornerShape(20.dp)
                            )
                            .padding(16.dp)
                    )

                    {
                        Text(
                            text = getString(R.string.description_notification_control),
                            style = TextStyle(fontSize = 14.sp),
                            color = colorResource(id = R.color.HeadText)
                        )
                    }
                }
            }

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

}//class END












