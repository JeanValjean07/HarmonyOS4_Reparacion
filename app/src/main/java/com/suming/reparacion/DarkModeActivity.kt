package com.suming.reparacion

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.graphics.scale
import androidx.core.view.WindowCompat
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.lifecycleScope
import com.suming.reparacion.ActivityComponents.DarkMode.DarkModeFragment
import com.suming.reparacion.AddonTools.showCustomToast
import com.suming.reparacion.DataPack.Descriptions
import com.suming.reparacion.FunctionalPack.BitmapLoader
import com.suming.reparacion.FunctionalPack.WallpaperFileWrapper
import com.suming.reparacion.FunctionalPack.WallpaperSetor
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

@Suppress("LocalVariableName")
class DarkModeActivity: AppCompatActivity() {
    //重复过滤器
    private val CoolDownGap_createShortcut = 4000L
    private var lastClickMillis: Long = 0
    //ContentArea
    private lateinit var NestedScrollArea: NestedScrollView

    //Lifecycle
    @SuppressLint("MissingInflatedId", "InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //界面配置
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dark_mode)
        //准备工作
        init()


        //TopBarCompose
        val TopBarCompose = findViewById<ComposeView>(R.id.TopBarCompose)
        TopBarCompose.setContent{
            ComposeRoot()
        }
        //ExplanationCompose
        val ExplanationCompose = findViewById<ComposeView>(R.id.ExplanationCompose)
        ExplanationCompose.setContent {
            ExplanationRoot()
        }



        //加载图片展示区
        initLoadWallpapers()

        //注册其他操作
        registerMoreActions()

        //主要操作按钮
        registerMainActions()


    }

    override fun onResume() {
        super.onResume()

    }



    //Composable Functions
    @Composable
    fun ComposeRoot() {
        //在root中取颜色模式
        isDarkMode = isSystemInDarkTheme()
        ColorPack = if (isDarkMode) DarkColorScheme else LightColorScheme
        //使用Box作为根布局
        Box(modifier = Modifier
            .wrapContentHeight()
            .fillMaxWidth()
            .background(Color.Transparent)) {

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


            //最顶层
            BrushArea()
            AdvancedTopBar(onHeightMeasured = { height ->
                //更新内边距
                updateNestTopPadding(height + statusBarHeight)
                //
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
                        //返回按钮
                        CircleButton(
                            onClick = { finish() },
                            backgroundColor = ColorPack.background.copy(alpha = 0.99f),
                            size = 40.dp,
                            border = BorderStroke(
                                width = 0.5.dp,
                                color = Color.Gray.copy(alpha = 0.1f)
                            ),
                            modifier = Modifier.padding(start = 15.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "退出",
                                modifier = Modifier.background(Color.Transparent),
                                tint = ColorPack.secondary
                            )
                        }
                        //标题文本
                        Text(
                            text = "深色模式壁纸",
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
                        //设置按钮
                        CircleButton(
                            onClick = { startSettingFragment() },
                            backgroundColor = ColorPack.background.copy(alpha = 0.99f),
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
                                modifier = Modifier.background(Color.Transparent),
                                tint = ColorPack.secondary
                            )
                        }
                    }
                }
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
    @Composable
    fun ContentCard(name: String, description: String, onClick: () -> Unit) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 3.dp)
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
                containerColor = ColorPack.background,
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
                    color = ColorPack.primary
                )
                //大小标题间距
                Spacer(modifier = Modifier.height(4.dp))
                //小标题或描述
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = ColorPack.secondary
                )
            }
        }
    }
    @Composable
    fun ExplanationRoot(){
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 10.dp, end = 10.dp, top = 5.dp, bottom = 15.dp)
                .uniformShadow()
                .clip(RoundedCornerShape(20.dp))
                .background(Color.Transparent),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(
                width = 0.5.dp,
                color = Color.Gray.copy(alpha = 0.1f)
            ),
            colors = CardDefaults.cardColors(
                containerColor = ColorPack.background,
            ),
            onClick = {}
        ){
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
                    .padding(10.dp)

            ) {
                Text(
                    text = Descriptions.textString_description_darkmodepaper,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = ColorPack.primary,
                )
            }
        }
    }
    //访问xml颜色表 colorResource(id = R.color.HeadText)

    //Functions
    //注册非必要按钮
    private fun registerMoreActions(){
        //点击图标彩蛋
        val DarkModeIcon = findViewById<ImageView>(R.id.sign_dark_mode)
        DarkModeIcon.setOnClickListener {
            notice("哼,哼,啊啊啊啊啊啊啊")
        }
    }
    //注册主要操作区
    private fun registerMainActions(){

        //点击图片区域弹出菜单
        imageViewDark = findViewById(R.id.imageDark)
        imageViewLight = findViewById(R.id.imageLight)
        imageViewDark?.setOnClickListener {
            showOptionMenu()
        }
        imageViewLight?.setOnClickListener {
            showOptionMenu()
        }


        //按钮：选择/更改深色壁纸
        val ButtonSelectDarkWp = findViewById<TextView>(R.id.buttonChangeDark)
        ButtonSelectDarkWp.setOnClickListener {
            consoleLog("开始选择深色壁纸")
            openGalleryToPick("dark")
        }
        //按钮：选择/更改浅色壁纸
        val ButtonSelectLightWp = findViewById<TextView>(R.id.buttonChangeLight)
        ButtonSelectLightWp.setOnClickListener {
            consoleLog("开始选择浅色壁纸")
            openGalleryToPick("light")
        }


        //按钮：返回桌面
        val ButtonSuperExit = findViewById<Button>(R.id.buttonSuperExit)
        ButtonSuperExit.setOnClickListener {
            moveTaskToBack(true)
            //待添加分支：返回桌面时结束进程
        }
        //按钮：切换到深色壁纸
        ButtonSwitchDark = findViewById(R.id.buttonSwitchDark)
        ButtonSwitchDark?.setOnClickListener {
            switchNow("dark")
        }
        //按钮：切换到浅色壁纸
        ButtonSwitchLight = findViewById(R.id.buttonSwitchLight)
        ButtonSwitchLight?.setOnClickListener {
            switchNow("light")
        }
        //按钮：添加快捷方式
        val ButtonAddTile = findViewById<Button>(R.id.buttonAddTile)
        ButtonAddTile.setOnClickListener {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastClickMillis < CoolDownGap_createShortcut) {
                return@setOnClickListener
            } else {
                lastClickMillis = currentTime
                notice("请确保开启了创建快捷方式权限丨您也可使用磁贴")
            }
            createShortcut()

        }
        //按钮：清除壁纸
        val ButtonClearWp = findViewById<Button>(R.id.buttonClear)
        ButtonClearWp.setOnClickListener {
            clearWallPaper()
            notice("已清除")
        }

    }
    //修改滚动区域顶部内边距
    private fun updateNestTopPadding(topPadding: Int){
        //consoleLog("updateNestTopPadding: 发起修改内边距")
        NestedScrollArea.setPadding(0, topPadding, 0, 0)
    }
    //调用图片选择器
    private var pickImageMode: String = ""
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            val selectedImageUri = result.data?.data
            selectedImageUri?.let { processAndSaveImage(it, pickImageMode) }
        }
    }
    private fun openGalleryToPick(mode: String) {
        pickImageMode = mode
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }
    //处理并保存图片
    private fun processAndSaveImage(uri: Uri, mode: String) {
        //infoBitmap：获取图片信息
        fun infoBitmap(bitmapForScale: Bitmap): Pair<Int, Int> {
            //获取图片原始分辨率
            val picWidth = bitmapForScale.width
            val picHeight = bitmapForScale.height

            return Pair(picWidth, picHeight)
        }
        //infoScreen：获取屏幕分辨率
        fun infoScreen(): Pair<Int, Int> {
            //获取屏幕分辨率
            var screenWidth: Int
            var screenHeight: Int
            val displayMetrics = DisplayMetrics()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val display = windowManager.currentWindowMetrics.bounds
                displayMetrics.widthPixels = display.width()
                displayMetrics.heightPixels = display.height()
                screenWidth = displayMetrics.widthPixels
                screenHeight = displayMetrics.heightPixels
            } else {
                @Suppress("DEPRECATION")
                val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
                val display = windowManager.defaultDisplay
                val realMetrics = DisplayMetrics()
                display.getRealMetrics(realMetrics)
                screenWidth = realMetrics.widthPixels
                screenHeight = realMetrics.heightPixels
            }

            return Pair(screenWidth, screenHeight)

        }
        //缩放并裁剪为屏幕分辨率
        fun scaleAndCropBitmap(bitmapForScale: Bitmap, picWidth: Int, picHeight: Int, screenWidth: Int, screenHeight: Int): Bitmap {
            consoleLog("DarkModeActivity: 开始把图片缩放并裁剪为手机屏幕分辨率")
            val heightRatio = screenHeight.toFloat() / picHeight.toFloat()
            val newWidth = (picWidth * heightRatio).toInt()
            val newHeight = (picHeight * heightRatio).toInt()
            var scaledBitmap: Bitmap? = bitmapForScale.scale(newWidth, newHeight)
            if( scaledBitmap == null ){
                consoleLog("DarkModeActivity: 严重错误 scaledBitmap == null")
                return bitmapForScale
            }
            val picWidth2 = scaledBitmap.width
            //分别处理
            if ( picWidth2 >= screenWidth ){
                val x: Int = ( picWidth2 / 2 ) - ( screenWidth / 2 )
                val y = 0
                scaledBitmap = Bitmap.createBitmap(scaledBitmap,x,y, screenWidth, screenHeight)
            }
            if( picWidth2 < screenWidth ){
                val widthRatio = screenWidth.toFloat() / picWidth.toFloat()
                val newWidth = ( picWidth * widthRatio ).toInt()
                val newHeight = ( picHeight * widthRatio ).toInt()
                scaledBitmap = bitmapForScale.scale( newWidth, newHeight )
                val picHeight2 = scaledBitmap.height
                val x = 0
                val y: Int = (picHeight2/2) - (screenHeight/2)
                scaledBitmap = Bitmap.createBitmap(scaledBitmap,x,y, screenWidth, screenHeight)
            }
            return scaledBitmap
        }
        //裁剪为微动尺寸
        fun clipBitmap(bitmapForScale: Bitmap, screenWidth: Int, screenHeight: Int): Bitmap {
            val x = 0
            val y = SettingsRequestCenter.get_VALUE_SlightMove(this@DarkModeActivity)
            val height = screenHeight - 2 * y

            return Bitmap.createBitmap(bitmapForScale,x,y, screenWidth,height)
        }

        //取出目标图片
        val originalBitmap =  contentResolver.openInputStream(uri)?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream)
        }
        //检查图片是否有效
        if (originalBitmap == null) {
            showCustomToast("图片读取失败")
            return
        }
        //创建副本
        val originalBitmapForScale = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
        //获取必要信息
        val (picWidth ,picHeight) = infoBitmap(originalBitmapForScale)
        val (screenWidth, screenHeight) = infoScreen()
        //裁剪为屏幕分辨率
        val croppedBitmap = scaleAndCropBitmap(originalBitmapForScale,picWidth,picHeight,screenWidth,screenHeight)
        //裁剪微动版本
        val clippedBitmap = clipBitmap(croppedBitmap,screenWidth,screenHeight)
        //保存图片到内部
        saveImageInternal(croppedBitmap, clippedBitmap, mode)


        //在设置清单中写入标志位
        when (mode) {
            "dark" -> {
                //保存标记
                SettingsRequestCenter.set_State_dark_paper_set(this@DarkModeActivity, true)
                //立即加载到预览视图
                loadImage("dark",push = true)
            }
            "light" -> {
                //保存标记
                SettingsRequestCenter.set_State_light_paper_set(this@DarkModeActivity, true)
                //立即加载到预览视图
                loadImage("light",push = true)
            }
        }

        //可选 - 保存图片到外部相册
        if (SettingsRequestCenter.get_PREFS_Save_Clip_Out(this@DarkModeActivity)) {
            saveImageToExternal(croppedBitmap, clippedBitmap)
        }
    }
    //保存图片到内部
    private fun saveImageInternal(croppedBitmap: Bitmap, clippedBitmap: Bitmap, mode: String){
        //定义文件实例
        val wallpaperFileWrapper = WallpaperFileWrapper()
        val (file,fileClipped) = wallpaperFileWrapper.wrapFile(this,mode = mode)

        //保存原图
        FileOutputStream(file).use { outputStream ->
            croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        }
        //保存裁剪后的图片
        FileOutputStream(fileClipped).use { outputStream ->
            clippedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        }

    }
    //保存图片到外部
    private fun saveImageToExternal(bitmap: Bitmap, clippedBitmap: Bitmap) {
        //保存原图
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "original.jpg")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/使用过的壁纸")
        }
        contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)?.let {
            val outputStream: OutputStream? = contentResolver.openOutputStream(it)
            outputStream?.use { stream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            }
        }
        //保存裁剪后的图片
        val clippedValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "clipped.jpg")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/使用过的壁纸")
        }
        contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, clippedValues)?.let {
            val outputStream: OutputStream? = contentResolver.openOutputStream(it)
            outputStream?.use { stream ->
                clippedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            }
        }
    }
    //加载本地图片
    @SuppressLint("CutPasteId")
    private fun loadImage(mode: String, needClipped: Boolean = false, push: Boolean = false): Pair<Boolean, Bitmap?> {
        val bitmapLoader = BitmapLoader()
        when(mode){
            //深色壁纸
            "dark" -> {
                consoleLog("loadImage: 开始加载深色壁纸")
                //直接取文件实例
                val wallpaperFileWrapper = WallpaperFileWrapper()
                val (file,fileClipped) = wallpaperFileWrapper.wrapFile(this,mode = mode)
                //找BitmapLoader请求
                var bitmap: Bitmap?
                if( needClipped){
                    bitmap = bitmapLoader.loadBitmap(mode,fileClipped).second
                }else{
                    bitmap = bitmapLoader.loadBitmap(mode,file).second
                }
                //
                if(bitmap == null){
                    consoleLog("loadImage: 加载深色壁纸失败")
                    return Pair(false,null)
                }else{
                    //推送到显示
                    if(push) pushToImageView(bitmap,mode)

                    return Pair(true,bitmap)
                }
            }
            //浅色壁纸
            "light" -> {
                consoleLog("loadImage: 开始加载浅色壁纸")
                //直接取文件实例
                val wallpaperFileWrapper = WallpaperFileWrapper()
                val (file,fileClipped) = wallpaperFileWrapper.wrapFile(this,mode = mode)
                //找BitmapLoader请求
                var bitmap: Bitmap?
                if( needClipped){
                    bitmap = bitmapLoader.loadBitmap(mode,fileClipped).second
                }else{
                    bitmap = bitmapLoader.loadBitmap(mode,file).second
                }
                //
                if(bitmap == null){
                    consoleLog("loadImage: 加载浅色壁纸失败")
                    return Pair(false,null)
                }else{
                    //推送到显示
                    if(push) pushToImageView(bitmap,mode)

                    return Pair(true,bitmap)
                }
            }
            //其他错误传参
            else -> {
                consoleLog("loadImage: 收到无效的参数,期望参数为\"dark\"或\"light\"")
                return Pair(false,null)
            }
        }
    }
    //推送到显示区域
    private var imageViewDark: ImageView? = null
    private var imageViewLight: ImageView? = null
    private fun pushToImageView(bitmap: Bitmap,mode: String){
        imageViewDark = findViewById(R.id.imageDark)
        imageViewLight = findViewById(R.id.imageLight)

        when(mode){
            "dark" -> {
                imageViewDark?.setImageBitmap(bitmap)
            }
            "light" -> {
                imageViewLight?.setImageBitmap(bitmap)
            }
        }
    }
    private fun clearImageView(){
        imageViewDark?.setImageBitmap(null)
        imageViewLight?.setImageBitmap(null)
        updateImageAreaActionText(0,0)
    }
    //清除壁纸(图片实例+标志位)
    private fun clearWallPaper() {
        //清除视图内图片
        clearImageView()
        //清理标志位
        SettingsRequestCenter.set_State_dark_paper_set(this@DarkModeActivity, false)
        SettingsRequestCenter.set_State_light_paper_set(this@DarkModeActivity, false)
        //删除图片实例
        val wallpaperFileWrapper = WallpaperFileWrapper()
        val img_directory = wallpaperFileWrapper.getImageDir()
        deletePaper(dir = img_directory)
    }
    private fun deletePaper(target: String = "",dir: String = "") {
        //Dir实例
        val dir = File(dir)
        //未传入参数时全删了
        if(target.isEmpty()){
            //直接全删了然后重新建文件夹
            if (dir.exists() && dir.isDirectory) {
                val success = dir.deleteRecursively()
                if (success) {
                    dir.mkdirs()
                }
            } else {
                dir.mkdirs()
            }
        }else{
            //根据传入参数删除对应文件
            val wallpaperFileWrapper = WallpaperFileWrapper()
            val (file,fileClipped) = wallpaperFileWrapper.wrapFile(this,mode = target)
            if (file.exists()) {
                file.delete()
            }
            if (fileClipped.exists()) {
                fileClipped.delete()
            }
        }
    }
    //初始化加载壁纸
    private fun initLoadWallpapers(){
        //读取标志位
        val darkSet = SettingsRequestCenter.get_State_dark_paper_set(this@DarkModeActivity)
        val lightSet = SettingsRequestCenter.get_State_light_paper_set(this@DarkModeActivity)
        //根据标志位加载壁纸
        if (darkSet){
            consoleLog("initLoadWallpapers: 加载深色壁纸")
            loadImage("dark",push = true)
        }else{
            consoleLog("initLoadWallpapers: 深色壁纸标记为未设置,跳过加载")
        }
        if (lightSet){
            consoleLog("initLoadWallpapers: 加载浅色壁纸")
            loadImage("light",push = true)
        }else{
            consoleLog("initLoadWallpapers: 浅色壁纸标记为未设置,跳过加载")
        }
        //更新操作区指示文字
        val dark = if (darkSet) 1 else 0
        val light = if (lightSet) 1 else 0
        updateImageAreaActionText(dark,light)

    }
    //修改操作区指示文字
    private var actionText_dark: TextView? = null
    private var actionText_light: TextView? = null
    private fun updateImageAreaActionText(dark: Int = -1,light: Int = -1){
        actionText_dark = findViewById(R.id.buttonChangeDark)
        actionText_light = findViewById(R.id.buttonChangeLight)
        //传入默认值时自己加载
        if (dark == -1){
            if (SettingsRequestCenter.get_State_dark_paper_set(this@DarkModeActivity)){
                actionText_dark?.text = "修改"
            }else{
                actionText_dark?.text = "设置"
            }
        }
        if (light == -1){
            if (SettingsRequestCenter.get_State_light_paper_set(this@DarkModeActivity)){
                actionText_light?.text = "修改"
            }else{
                actionText_light?.text = "设置"
            }
        }
        //传入具体值时直接设置
        if (dark == 0){
            actionText_dark?.text = "选择"
        }else if(dark == 1){
            actionText_dark?.text = "修改"
        }
        if (light == 0){
            actionText_light?.text = "选择"
        }else if(light == 1){
            actionText_light?.text = "修改"
        }

    }
    //弹出选项菜单
    private fun showOptionMenu(){

    }
    //打开设置面板
    private fun startSettingFragment(){
        //打开DarkModeFragment
        val fragment = DarkModeFragment()
        fragment.show(supportFragmentManager, "DarkModeFragment")
    }
    //设置系统壁纸核心方法
    private fun applySystemWallpaper(bitmap: Bitmap){
        val wallpaperSetor = WallpaperSetor()
        wallpaperSetor.applySystemWallpaper(bitmap, this)
    }


    //功能执行函数
    //倒计时后退出到桌面
    private fun autoFinish(){
        //先退回桌面
        lifecycleScope.launch {
            delay(2000)
            moveTaskToBack(true)
            //结束进程
            if(SettingsRequestCenter.get_PREFS_End_Process_After(this@DarkModeActivity)){
                val pid = android.os.Process.myPid()
                android.os.Process.killProcess(pid)
            }
        }
    }
    //立即切换到指定模式壁纸
    @OptIn(DelicateCoroutinesApi::class)
    private var ButtonSwitchDark: Button? = null
    private fun switchNowToDark(exitAfter: Boolean = false){
        //先确认有没有图,修改按钮提示文字
        if (SettingsRequestCenter.get_State_dark_paper_set(this@DarkModeActivity)) {
            //再确认文件是否存在
            val needClipped = SettingsRequestCenter.get_PREFS_SlightMove_Clip(this)
            val (success,bitmap) = loadImage("dark",needClipped)
            if (success){
                if (bitmap != null){
                    //应用到系统
                    setButtonInfo("dark","正在应用深色壁纸,请稍等",true)
                    lifecycleScope.launch (Dispatchers.IO){
                        //执行应用
                        applySystemWallpaper(bitmap)
                        //自动退出
                        if (exitAfter) autoFinish()
                    }
                }else{
                    setButtonInfo("dark","深色壁纸读取失败",true)
                    endActionGapJob()
                }
            }else{
                setButtonInfo("dark","深色壁纸读取失败",true)
                endActionGapJob()
            }
        }else{
            setButtonInfo("dark","您似乎并未设置深色壁纸",false)
            noPaperNoticeJob()
        }
    }
    private var ButtonSwitchLight: Button? = null
    private fun switchNowToLight(exitAfter: Boolean = false){
        //先确认有没有图,修改按钮提示文字
        if (SettingsRequestCenter.get_State_light_paper_set(this@DarkModeActivity)) {
            //再确认文件是否存在
            val needClipped = SettingsRequestCenter.get_PREFS_SlightMove_Clip(this)
            val (success,bitmap) = loadImage("light",needClipped)
            if (success){
                if (bitmap != null){
                    //应用到系统
                    setButtonInfo("light","正在应用浅色壁纸,请稍等",true)
                    //
                    lifecycleScope.launch (Dispatchers.IO){
                        //执行应用
                        applySystemWallpaper(bitmap)
                        //自动退出
                        if (exitAfter) autoFinish()
                    }
                }else{
                    setButtonInfo("light","浅色壁纸读取失败",true)
                    endActionGapJob()
                }
            }else{
                setButtonInfo("light","浅色壁纸读取失败",true)
                endActionGapJob()
            }
        }else{
            setButtonInfo("light","您似乎并未设置浅色壁纸",false)
            noPaperNoticeJob()
        }
    }
    //立即切换到指定模式壁纸入口
    private fun switchNow(mode: String) {
        //锁
        if (state_paper_apply_running) {
            showCustomToast("请勿重复点击")
            return
        }
        actionGapJob()
        //执行切换
        if (mode == "dark") {
            switchNowToDark(exitAfter = true)
        } else if (mode == "light") {
            switchNowToLight(exitAfter = true)
        }
    }
    //执行间隔控制
    private var state_paper_apply_running = false
    private var actionGapJob: Job? = null
    private fun actionGapJob() {
        actionGapJob?.cancel()
        state_paper_apply_running = true
        actionGapJob = lifecycleScope.launch {
            delay(3000)
            resetButtonInfo()
            state_paper_apply_running = false
        }
    }
    private fun endActionGapJob(){
        actionGapJob?.cancel()
        state_paper_apply_running = false
    }
    //未设置壁纸控制
    private var noPaperNoticeJob: Job? = null
    private fun noPaperNoticeJob(){
        noPaperNoticeJob?.cancel()
        noPaperNoticeJob = lifecycleScope.launch {
            delay(2000)
            resetButtonInfo()
        }
    }
    //重置按钮
    private fun resetButtonInfo(){
        //切换文字
        ButtonSwitchDark?.text = getString(R.string.dark_mode_paper_apply_notice_dark)
        ButtonSwitchLight?.text = getString(R.string.dark_mode_paper_apply_notice_light)
        //切换按钮背景颜色
        ButtonSwitchDark?.backgroundTintList = ContextCompat.getColorStateList(this, R.color.ButtonBg)
        ButtonSwitchLight?.backgroundTintList = ContextCompat.getColorStateList(this, R.color.ButtonBg)
    }
    //设置按钮
    private fun setButtonInfo(target: String,text:String,highlight: Boolean = false){
        //切换文字
        when(target){
            "dark" -> {
                ButtonSwitchDark?.text = text
                if(highlight){
                    ButtonSwitchDark?.backgroundTintList = ContextCompat.getColorStateList(this, R.color.ButtonBgHighlight)
                }
            }
            "light" -> {
                ButtonSwitchLight?.text = text
                if(highlight){
                    ButtonSwitchLight?.backgroundTintList = ContextCompat.getColorStateList(this, R.color.ButtonBgHighlight)
                }
            }
        }

    }
    //创建桌面快捷方式
    private fun createShortcut(){
        //
        val shortcutManager = getSystemService(ShortcutManager::class.java)
        val shortcutIntent = Intent(this, DarkModePure::class.java).apply {
            action = Intent.ACTION_MAIN
            putExtra("FROM_HOME",true)
        }
        val shortcut = ShortcutInfo.Builder(this, "dark_mode_shortcut")
            .setShortLabel("切换壁纸")
            .setLongLabel("根据当前模式切换壁纸")
            .setIcon(Icon.createWithResource(this, R.drawable.ic_shortcut))
            .setIntent(shortcutIntent)
            .build()

        shortcutManager?.requestPinShortcut(shortcut, null)
    }



    //集中初始化
    @OptIn(DelicateCoroutinesApi::class)
    private fun init() {
        //共享视图初始化
        NestedScrollArea = findViewById(R.id.NestedScrollArea)

    }
    //显示短通知
    private fun notice(text: String) {
        showCustomToast(text)
    }
    //统一日志控制
    private fun consoleLog(msg: String, mark: Boolean = true) {
        if (mark) {
            Log.d("SuMing", msg)
        }
    }


}




