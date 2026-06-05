package com.suming.reparacion

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SnippetFolder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.suming.reparacion.data.ToolList
import com.suming.reparacion.data.ToolPackage
import com.suming.reparacion.HelperTools.showCustomToast
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Suppress("LocalVariableName")
class MainActivity : AppCompatActivity() {

    //连接到ViewModel
    val mainViewModel = MainViewModel()

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //界面配置
        enableEdgeToEdge()
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        //手动设置状态栏字体颜色
        setStatusBarFontColor()


        //工具列表
        val toolsList = ToolList.toolsList

        //托管给ComposableRoot
        setContent {
            ComposeRoot(toolsList, mainViewModel)
        }

    }

    @Composable
    fun ComposeRoot(toolsList: List<ToolPackage>, mainViewModel: MainViewModel) {
        //使用Box作为根布局
        Box(modifier = Modifier.fillMaxSize()) {

            //顶部栏高度值
            val statusBarHeight = WindowInsets.statusBars.getTop(LocalDensity.current)
            var topBarHeight by remember { mutableIntStateOf(300) }
            val topPaddingDp = with(LocalDensity.current) {
                (statusBarHeight + topBarHeight).toDp()
            }

            //顶部栏高度值动画 也可不使用动画单纯传值
            //曲线可选 CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)
            val animatedTopPadding by animateDpAsState(
                targetValue = topPaddingDp,
                animationSpec = tween(
                    durationMillis = 300,
                    easing = FastOutSlowInEasing
                )
            )

            //最底层
            GlobalBackPic()

            //
            ToolsListColumn(toolsList, topPaddingDp )


            //最顶层
            BrushArea()
            AdvancedTopBar(onHeightMeasured = { height ->
                    topBarHeight = height
                })
        }


    }
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

            val darkTheme: Boolean = isSystemInDarkTheme()
            val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

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
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircleButton(
                            onClick = { exitApp() },
                            backgroundColor = colorScheme.background.copy(alpha = 0.99f),
                            size = 40.dp,
                            border = BorderStroke(
                                width = 0.5.dp,
                                color = Color.Gray.copy(alpha = 0.1f)
                            ),
                            modifier = Modifier.padding(start = 15.dp)
                        ) {
                            Icon(
                                Icons.Filled.Clear,
                                contentDescription = "退出",
                                modifier = Modifier.background(Color.Transparent),
                                tint = colorScheme.secondary
                            )
                        }

                        Text(
                            text = "补全计划",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.primary,
                            modifier = Modifier.padding(start = 5.dp)
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircleButton(
                            onClick = { startGuide() },
                            backgroundColor = colorScheme.background.copy(alpha = 0.99f),
                            size = 40.dp,
                            border = BorderStroke(
                                width = 0.5.dp,
                                color = Color.Gray.copy(alpha = 0.1f)
                            ),
                            modifier = Modifier.padding(end = 2.dp)
                        ) {
                            Icon(
                                Icons.Filled.SnippetFolder,
                                contentDescription = "指南",
                                modifier = Modifier.background(Color.Transparent),
                                tint = colorScheme.secondary
                            )
                        }

                        CircleButton(
                            onClick = { startSetting() },
                            backgroundColor = colorScheme.background.copy(alpha = 0.99f),
                            size = 40.dp,
                            border = BorderStroke(
                                width = 0.5.dp,
                                color = Color.Gray.copy(alpha = 0.1f)
                            ),
                            modifier = Modifier.padding(end = 15.dp)
                        ) {
                            Icon(
                                Icons.Filled.Settings,
                                contentDescription = "设置",
                                tint = colorScheme.secondary
                            )
                        }
                    }

                }

                //调试用线
                /*
                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth(),
                    thickness = 0.2.dp,
                    color = Color.Gray.copy(alpha = 0.1f)
                )

                 */
            }

        }
    }
    @Composable
    fun CircleButton( onClick: () -> Unit,
                      modifier: Modifier = Modifier,
                      size: Dp = 30.dp,
                      backgroundColor: Color = MaterialTheme.colorScheme.primary,
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
                    spotColor = Color.Black.copy(alpha = 0.4f),  // 控制阴影颜色
                    ambientColor = Color.Black.copy(alpha = 0.4f)
                )
                .then(if (border != null) Modifier.border(border, CircleShape) else Modifier)
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
    fun BrushArea(modifier: Modifier = Modifier, height: Dp = 110.dp) {
        val darkTheme: Boolean = isSystemInDarkTheme()
        val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
        //
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(height)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            colorScheme.surface.copy(alpha = 0.90f),
                            colorScheme.surface.copy(alpha = 0.80f),
                            colorScheme.surface.copy(alpha = 0.60f),
                            colorScheme.surface.copy(alpha = 0.30f),
                            colorScheme.surface.copy(alpha = 0f)
                        ),
                        startY = 30f,
                    )
                )
        )
    }
    @Composable
    fun GlobalBackPic(){
        //放置一个全屏底部区域，未来支持设置自定义壁纸

        val darkTheme: Boolean = isSystemInDarkTheme()
        val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(colorScheme.surface)
        )


    }
    @SuppressLint("UnrememberedMutableState")
    @Composable
    fun ToolsListColumn(toolsList: List<ToolPackage>, animatedTopPadding: Dp) {
        //指定边距
        val contentPadding by derivedStateOf {
            PaddingValues(
                top = animatedTopPadding,
                bottom = 400.dp,
                start = 0.dp,
                end = 0.dp
            )
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            items(toolsList) { tool ->
                ToolCard(
                    name = tool.name,
                    description = tool.description,
                    onClick = { onClickListItem(tool.intent) }
                )
            }
        }
    }
    @Composable
    fun ToolCard(name: String, description: String, onClick: () -> Unit) {
        val darkTheme: Boolean = isSystemInDarkTheme()
        val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 3.dp)
                //.shadow(elevation = 5.dp, shape = RoundedCornerShape(12.dp), clip = false, spotColor = Color.Black.copy(alpha = 0.2f), ambientColor = Color.Black.copy(alpha = 1f))
                .uniformShadow()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            shape = RoundedCornerShape(15.dp),
            border = BorderStroke(
                width = 0.5.dp,
                color = Color.Gray.copy(alpha = 0.1f)
            ),
            colors = CardDefaults.cardColors(
                containerColor = colorScheme.background,
            ),
            onClick = onClick
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(13.dp)
            ) {
                //大标题
                Text(
                    text = name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = colorScheme.primary
                )
                //大小标题间距
                Spacer(modifier = Modifier.height(4.dp))
                //小标题或描述
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = colorScheme.secondary
                )
            }
        }
    }
    //自定义阴影
    @Suppress("DEPRECATION")
    fun Modifier.uniformShadow(
        blurRadius: Float = 15f,
        shadowColor: Color = Color.Black.copy(alpha = 0.1f)
    ) = this.drawBehind {
        drawIntoCanvas { canvas ->
            val paint = Paint().apply {
                color = shadowColor
                asFrameworkPaint().maskFilter = android.graphics.BlurMaskFilter(
                    blurRadius,
                    android.graphics.BlurMaskFilter.Blur.NORMAL
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

    //处理卡片点击事件
    private fun onClickListItem(toolIntent: String) {
        consoleLog("接收到卡片点击事件丨事件意图字段：$toolIntent")

        //执行
        when(toolIntent){
            "MANAGER_INTENT_DARK_MODE_WALLPAPER_SWITCH" -> {
                startActivity(Intent(this, DarkModeActivity::class.java))
            }
            "MANAGER_INTENT_NOTIFICATION_MANAGER" -> {
                startActivity(Intent(this, NotificationManager::class.java))
            }
            "MANAGER_INTENT_VOLUME_CONTROL" -> {
                startActivity(Intent(this, VolumeControl::class.java))
            }

            "MANAGER_INTENT_NONE" -> {
                showCustomToast("功能开发中")
            }

        }

    }
    //启动设置页面
    private fun startSetting() {
        startActivity(Intent(this, SettingsActivity::class.java))
    }
    //启动指南
    private fun startGuide() {
        startActivity(Intent(this, GuidanceActivity::class.java))
    }
    //退出应用
    private fun exitApp() {
        consoleLog("主动退出应用")
        //
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



    //深浅色模式检测+设置状态栏字体颜色
    fun colorModeDetector(context: Context): Boolean {
        return when (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_NO -> true
            Configuration.UI_MODE_NIGHT_YES -> false
            else -> true
        }
    }
    fun setStatusBarFontColor() {
        val insetsController = WindowInsetsControllerCompat(
            window, window.decorView
        )
        insetsController.isAppearanceLightStatusBars = colorModeDetector(this)
    }
    //统一日志控制
    private fun consoleLog(msg: String, mark: Boolean = true) {
        if (mark) {
            Log.d("SuMing", msg)
        }
    }

}