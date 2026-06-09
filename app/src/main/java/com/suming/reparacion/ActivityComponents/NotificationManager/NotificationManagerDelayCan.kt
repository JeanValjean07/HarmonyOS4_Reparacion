package com.suming.reparacion.ActivityComponents.NotificationManager

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.graphics.BlurMaskFilter
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.suming.reparacion.DataPack.Connect
import com.suming.reparacion.DataPack.NotificationPack
import com.suming.reparacion.R
import kotlinx.coroutines.launch

class NotificationManagerDelayCan: DialogFragment() {
    companion object {
        fun newInstance(): NotificationManagerDelayCan = NotificationManagerDelayCan().apply { arguments =
            bundleOf()
        }
    }



    @Suppress("DEPRECATION")
    override fun onStart() {
        super.onStart()
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE){
            //横屏时隐藏状态栏
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                ViewCompat.setOnApplyWindowInsetsListener(dialog?.window?.decorView ?: return) { view, insets -> WindowInsetsCompat.CONSUMED }

                /*
                dialog?.window?.decorView?.post { dialog?.window?.insetsController?.let { controller ->
                    controller.hide(WindowInsets.Type.statusBars())
                    controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                } }

                 */

                //三星专用:显示到挖空区域
                dialog?.window?.attributes?.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            } else {
                dialog?.window?.decorView?.systemUiVisibility = (
                        View.SYSTEM_UI_FLAG_FULLSCREEN
                                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        )
            }
            //
            dialog?.window?.setWindowAnimations(R.style.ANIM_DialogFragment_SlideInOutHorizontal)
            dialog?.window?.setDimAmount(0.1f)
            dialog?.window?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            dialog?.window?.statusBarColor = Color(0x00000000).toArgb()
            dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        }
        else if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT){
            dialog?.window?.setWindowAnimations(R.style.ANIM_DialogFragment_SlideInOut)
            dialog?.window?.setDimAmount(0.1f)
            dialog?.window?.statusBarColor = Color(0x00000000).toArgb()
            dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

            if(context?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_NO){
                val decorView: View = dialog?.window?.decorView ?: return
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //设置Fragment主题
        setStyle(STYLE_NO_TITLE, R.style.BASIC_FRAGMENT_NO_BAR)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?, ): View? {
        return inflater.inflate(R.layout.main_general_fragment, container, false)
    }

    @SuppressLint("UseGetLayoutInflater", "InflateParams", "SetTextI18n", "ClickableViewAccessibility", "CutPasteId")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //集中初始化
        init(view)

        //观察通知列表
        //需要在compose函数中发起观察
        //发起收集
        NotificationManagerRepo.setServiceConnect(Connect.service_intent_get_delay_list)

        //设置composeRoot
        ComposeRoot.setContent {
            ComposeRoot()
        }

        //系统手势监听
        lifecycleScope.launch {
            //监听返回手势
            dialog?.setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                    dismiss()
                    return@setOnKeyListener true
                }
                return@setOnKeyListener false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        consoleLog("Fragment onResume")
        checkPermission()
    }

    //Lifecycle Functions
    private fun init(view: View){
        //初始化composeRoot
        ComposeRoot = view.findViewById(R.id.fragment_compose_root)
        //设置卡片高度
        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT){
            ComposeRoot.layoutParams.height = (resources.displayMetrics.heightPixels * 0.8).toInt()
        }
    }



    //Compose Functions
    @Composable
    fun ComposeRoot() {
        //在root中取颜色模式
        isDarkMode = isSystemInDarkTheme()
        ColorPack = if (isDarkMode) DarkColorScheme else LightColorScheme
        //使用Box作为根布局
        Box(
            modifier = Modifier.Companion
                .fillMaxSize()
                .background(ColorPack.surface)
        ) {

            //顶部栏高度值
            val statusBarHeight = 0
            var topBarHeight by remember { mutableIntStateOf(0) }
            val topPaddingDp = with(LocalDensity.current) {
                    (statusBarHeight + topBarHeight).toDp()
            }


            //最底层

            //content
            ContentRoot(topPaddingDp)


            //最顶层
            BrushArea()
            AdvancedTopBar(onHeightMeasured = { height ->
                //更新内边距
                topBarHeight = height
            })
        }
    }
    private lateinit var ComposeRoot: ComposeView
    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @Composable
    fun AdvancedTopBar(onHeightMeasured: (height: Int) -> Unit) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(60.dp)
                .onGloballyPositioned { coordinates ->
                    onHeightMeasured(coordinates.size.height)
                },
            color = Color.Transparent,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(59.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    //左侧
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        //关闭按钮
                        CircleButton(
                            onClick = { dismiss() },
                            backgroundColor = ColorPack.background.copy(alpha = 0.99f),
                            size = 40.dp,
                            border = BorderStroke(
                                width = 0.5.dp,
                                color = Color.Gray.copy(alpha = 0.1f)
                            ),
                            modifier = Modifier.padding(start = 10.dp)
                        ) {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = "关闭",
                                modifier = Modifier.background(Color.Transparent),
                                tint = ColorPack.secondary
                            )
                        }
                        //标题文本
                        Text(
                            text = "已隐藏/延后的通知",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorPack.primary,
                            modifier = Modifier.padding(start = 0.dp)
                        )
                    }
                    //右侧
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                    }
                }
            }
        }
    }
    @Composable
    fun CircleButton(onClick: () -> Unit,
                     modifier: Modifier = Modifier,
                     size: Dp = 30.dp,
                     backgroundColor: Color = ColorPack.primary,
                     gradient: Brush? = null,
                     border: BorderStroke? = null,
                     elevation: Dp = 3.dp,
                     enabled: Boolean = true,
                     content: @Composable () -> Unit ) {
        val backgroundModifier = when {
            gradient != null -> Modifier.background(gradient)
            else -> Modifier.background(backgroundColor)
        }
        Box(
            modifier = modifier
                .size(size)
                .shadow(
                    elevation = elevation,
                    shape = CircleShape,
                    clip = false,
                    spotColor = Color.Black.copy(alpha = 0.4f),
                    ambientColor = Color.Black.copy(alpha = 0.4f)
                )
                .then(
                    if (border != null) Modifier.border(
                        border,
                        CircleShape
                    ) else Modifier.Companion
                )
                .clip(CircleShape)
                .then(backgroundModifier)
                .clickable(
                    enabled = enabled,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(
                        bounded = true,
                        color = Color.Gray
                    )
                ) { onClick() },
            contentAlignment = Alignment.Center,
        ) {
            content()
        }
    }
    @Composable
    fun BrushArea(modifier: Modifier = Modifier, height: Dp = 90.dp) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(height)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            ColorPack.surface.copy(alpha = 0.90f),
                            ColorPack.surface.copy(alpha = 0.0f)
                        ),
                    )
                )
        )
    }
    @Composable
    fun ContentRoot(topBarHeight: Dp){
        //观察通知列表
        val noticeList by NotificationManagerRepo.delayList.collectAsState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                //.verticalScroll(rememberScrollState())
        ) {
            NoticeListColumn(noticeList, topBarHeight)
        }
    }
    @SuppressLint("UnrememberedMutableState")
    @Composable
    fun NoticeListColumn(noticeList: List<NotificationPack>, animatedTopPadding: Dp) {
        //指定边距
        val contentPadding by derivedStateOf {
            PaddingValues(
                top = animatedTopPadding,
                bottom = 200.dp,
                start = 0.dp,
                end = 0.dp
            )
        }
        //点击菜单
        var selectedUUID by remember { mutableStateOf<String?>(null) }
        var showMenu by remember { mutableStateOf(false) }
        LazyColumn(
            modifier = Modifier.Companion.fillMaxSize(),
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            items(noticeList) { notice ->
                Box(
                    modifier = Modifier.Companion.fillMaxWidth()
                ) {
                    NoticeCard(
                        packageName = notice.packageName,
                        title = notice.title,
                        text = notice.text,
                        onClick = { selectedUUID = notice.uniqueID; showMenu = true }
                    )
                    if (selectedUUID == notice.uniqueID) {
                        DropdownMenu(
                            expanded = true,
                            onDismissRequest = { selectedUUID = null },
                            offset = DpOffset(
                                x = 100.dp,
                                y = 0.dp
                            ),
                            modifier = Modifier.Companion.wrapContentSize()
                                .background(ColorPack.background)
                        ) {
                            Text(
                                text = "ID $selectedUUID",
                                fontSize = 10.sp,
                                color = ColorPack.secondary,
                                modifier = Modifier.padding(
                                    horizontal = 10.dp,
                                    vertical = 3.dp
                                )
                            )
                            Text(
                                text = "包名 ${notice.packageName}",
                                fontSize = 10.sp,
                                color = ColorPack.secondary,
                                modifier = Modifier.padding(
                                    horizontal = 10.dp,
                                    vertical = 3.dp
                                )
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "取消延后/隐藏",
                                        fontSize = 12.sp,
                                        color = ColorPack.primary
                                    )
                                },
                                onClick = {
                                    //取消延后/隐藏
                                    cancelHideNotification(notice.key)
                                    //关闭菜单
                                    selectedUUID = null
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "进入系统通知设置",
                                        fontSize = 12.sp,
                                        color = ColorPack.primary
                                    )
                                },
                                onClick = {
                                    //打开系统通知设置页面
                                    openNotificationSetting(notice.packageName)
                                    //关闭菜单
                                    selectedUUID = null
                                }
                            )
                        }
                    }
                }
            }
        }
    }
    @Composable
    fun NoticeCard(packageName: String, title: String, text: String, onClick: () -> Unit) {
        Card(
            modifier = Modifier.Companion
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 3.dp)
                .uniformShadow()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Companion.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(15.dp),
            border = BorderStroke(
                width = 0.5.dp,
                color = Color.Companion.Gray.copy(alpha = 0.1f)
            ),
            colors = CardDefaults.cardColors(containerColor = ColorPack.background,),
            onClick = onClick
        ) {
            Column(
                modifier = Modifier.Companion.fillMaxWidth().padding(13.dp)
            ) {
                //App名称
                Text(
                    text = packageName,
                    fontSize = 9.sp,
                    color = ColorPack.secondary
                )
                //大小标题间距
                Spacer(modifier = Modifier.Companion.height(4.dp))
                //大标题
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Companion.Medium,
                    color = ColorPack.primary
                )
                //大小标题间距
                Spacer(modifier = Modifier.Companion.height(4.dp))
                //小标题或描述
                Text(
                    text = text,
                    fontSize = 12.sp,
                    color = ColorPack.secondary
                )
            }
        }
    }
    //自定义阴影
    @Suppress("DEPRECATION")
    fun Modifier.uniformShadow(
        blurRadius: Float = 15f,
        shadowColor: Color = Color.Companion.Black.copy(alpha = 0.1f)
    ) = this.drawBehind {
        drawIntoCanvas { canvas ->
            val paint = Paint().apply {
                color = shadowColor
                asFrameworkPaint().maskFilter = BlurMaskFilter(
                    blurRadius,
                    BlurMaskFilter.Blur.NORMAL
                )
            }

            canvas.drawRoundRect(
                left = 0f,
                top = 0f,
                right = size.width,
                bottom = size.height,
                radiusX = 12.dp.toPx(),
                radiusY = 12.dp.toPx(),
                paint = paint
            )
        }
    }
    //composable颜色配置
    private var isDarkMode: Boolean = false
    private lateinit var ColorPack: ColorScheme
    private val LightColorScheme = lightColorScheme(
        //全局底色
        surface = Color(0xFFFFFFFF),
        //一级和二级文字
        primary = Color(0xFF000000),
        secondary = Color(0xFF313131),
        //卡片底色
        background = Color(0xFFFFFFFF),

        )
    private val DarkColorScheme = darkColorScheme(
        //全局底色
        surface = Color(0xFF000000),
        //一级和二级文字
        primary = Color(0xFFFFFFFF),
        secondary = Color(0xFFF6F6F6),
        //卡片底色
        background = Color(0xFF121212),
    )

    //取消隐藏通知
    private fun cancelHideNotification(key: String){
        NotificationManagerRepo.cancelDelayNotification(key)
    }
    //打开系统通知设置页面
    private fun openNotificationSetting(packageName: String){
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
        startActivity(intent)
    }


    //检查权限
    private fun changePermissionState() {
        //跳转到设置页面
        val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
    private var isPermissionGranted by mutableStateOf(false)
    private fun checkPermission(){
        val enabledListenerPackages = NotificationManagerCompat.getEnabledListenerPackages(requireContext())
        val isNotificationListenerEnabled = enabledListenerPackages.contains(requireContext().packageName)
        if(isNotificationListenerEnabled){
            isPermissionGranted = true
        }else{
            isPermissionGranted = false
        }
    }
    //清除通知
    private fun clearNotification(reload: Boolean){
        NotificationManagerRepo.clearAll()
        if(reload){
            gatherCurrentNotification()
        }
        dismiss()
    }
    //重新读取通知
    private fun gatherCurrentNotification(){
        NotificationManagerRepo.setServiceConnect("SERVICE_INTENT_FETCH_ALL")
    }

    //统一日志控制
    private fun consoleLog(msg: String, mark: Boolean = true) {
        if (mark) {
            Log.d("SuMing", msg)
        }
    }


}