package com.suming.cpa

import android.annotation.SuppressLint
import android.app.Dialog
import android.app.WallpaperManager
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
import android.os.Process
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.cardview.widget.CardView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.graphics.scale
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

@Suppress("LocalVariableName")
class DarkModeActivity: AppCompatActivity() {
    //数值设置区
    private lateinit var Switch1: SwitchCompat
    private lateinit var Switch2: SwitchCompat
    private val COOLDOWN_TIME_2 = 4000L
    private var lastClickTime: Long = 0
    private var killWhenExit = 0

    //深色或浅色标志位
    private var mode_pictureSelect = ""

    //点图遮罩标志位
    private var DarkCoverShowing = false
    private var LightCoverShowing = false

    @SuppressLint("MissingInflatedId", "InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dark_mode)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_darkmode)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //准备工作
        preCheck()


        //按钮：退出
        val ButtonExit = findViewById<ImageButton>(R.id.buttonToolbarExit)
        ButtonExit.setOnClickListener {
            finish()
        }
        //Notice卡片点击时关闭
        val NoticeCard = findViewById<CardView>(R.id.noticeCard)
        NoticeCard.setOnClickListener {
            NoticeCard.visibility = View.GONE
        }
        //点击彩蛋
        val SignDarkMode = findViewById<ImageView>(R.id.sign_dark_mode)
        SignDarkMode.setOnClickListener {
            notice("哼,哼,啊啊啊啊啊啊啊",1000)
        }



        //开关：将选择的壁纸保存到外部
        Switch1 = findViewById(R.id.switchToGallery)
        Switch1.setOnCheckedChangeListener { _, isChecked ->
            saveSwitchState("saveToPublic", isChecked)
        }
        restoreSwitchState("saveToPublic")
        //开关：壁纸slightMove  //注意：华为的安卓10需要拦截此功能
        Switch2 = findViewById(R.id.switchSlightMove)
        Switch2.setOnCheckedChangeListener { _, isChecked ->
            val isHuaweiAndroid10: Boolean = Build.MANUFACTURER.equals("huawei", ignoreCase = true) && Build.VERSION.SDK_INT == Build.VERSION_CODES.Q
            if (isHuaweiAndroid10) {
                notice("由于华为在安卓10上魔改了壁纸管理器,此功能不可用",10000)
                return@setOnCheckedChangeListener
            }
            saveSwitchState("slightmove", isChecked)
        }
        restoreSwitchState("slightmove")

        //主区域按钮
        //按钮：选择/更改深色壁纸
        val ButtonSelectDarkWp = findViewById<TextView>(R.id.buttonChangeDark)
        ButtonSelectDarkWp.setOnClickListener {
            mode_pictureSelect = "dark"
            openGallery()
        }
        //按钮：选择/更改浅色壁纸
        val ButtonSelectLightWp = findViewById<TextView>(R.id.buttonChangeLight)
        ButtonSelectLightWp.setOnClickListener {
            mode_pictureSelect = "light"
            openGallery()
        }
        //按钮：返回桌面
        val ButtonSuperExit = findViewById<Button>(R.id.buttonSuperExit)
        ButtonSuperExit.setOnClickListener {
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_HOME)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            exit(100)
        }
        //按钮：切换深色壁纸
        val ButtonSwitchDark = findViewById<Button>(R.id.buttonSwitchDark)
        ButtonSwitchDark.setOnClickListener {
            switchNow("dark")
        }
        //按钮：切换浅色壁纸
        val ButtonSwitchLight = findViewById<Button>(R.id.buttonSwitchLight)
        ButtonSwitchLight.setOnClickListener {
            switchNow("light")
        }
        //按钮：添加快捷方式
        val ButtonAddTile = findViewById<Button>(R.id.buttonAddTile)
        ButtonAddTile.setOnClickListener {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastClickTime < COOLDOWN_TIME_2) {
                return@setOnClickListener
            } else {
                lastClickTime = currentTime
                notice("请确保开启了创建快捷方式权限丨您也可使用磁贴",5000)
            }
            createShortcut()

        }
        //按钮：清除壁纸
        val ButtonClearWp = findViewById<Button>(R.id.buttonClear)
        ButtonClearWp.setOnClickListener {
            clearWallPaper()
            notice("已清除",2000)
        }
        //按钮：设置微动值
        val ButtonSetValue = findViewById<Button>(R.id.buttonSetValue)
        ButtonSetValue.setOnClickListener() {
            val dialog = Dialog(this)
            val dialogView = LayoutInflater.from(this).inflate(R.layout.activity_dark_mode_settings, null)
            dialog.setContentView(dialogView)
            val input: EditText = dialogView.findViewById(R.id.dialog_input)
            val button: Button = dialogView.findViewById(R.id.dialog_button)
            button.setOnClickListener {
                val userInput = input.text.toString()
                setValue(userInput)
                dialog.dismiss()
            }
            dialog.show()
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            CoroutineScope(Dispatchers.Main).launch {
                delay(100)
                input.requestFocus()
                imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT)
            }

        }
        //按钮：立即设置（深色）（点击图片）
        val CardViewDark = findViewById<CardView>(R.id.cardViewDark)
        CardViewDark.setOnClickListener {
            if (!DarkCoverShowing) {
                DarkCoverShowing = true
                val ButtonSetDarkFrame = findViewById<FrameLayout>(R.id.buttonSetDarkFrame)
                ButtonSetDarkFrame.visibility = View.VISIBLE
                val sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
                if (sharedPreferences.contains("is_dark_wallpaper_set?")) {
                    val ButtonSetDark = findViewById<TextView>(R.id.buttonSetDark)
                    ButtonSetDark.text = "立即设置壁纸"
                }else{
                    val ButtonSetDark = findViewById<TextView>(R.id.buttonSetDark)
                    ButtonSetDark.text = "立即选择图片"
                }
            }else{
                DarkCoverShowing = false
                val ButtonSetDarkFrame = findViewById<FrameLayout>(R.id.buttonSetDarkFrame)
                ButtonSetDarkFrame.visibility = View.GONE
            }
        }
        val ButtonSetDark = findViewById<TextView>(R.id.buttonSetDark)
        ButtonSetDark.setOnClickListener {
            if (ButtonSetDark.text == "立即选择图片") {
                mode_pictureSelect = "dark"
                openGallery()
            }else{
                switchNow("dark")
            }
        }
        //按钮：立即设置（浅色）（点击图片）
        val CardViewLight = findViewById<CardView>(R.id.cardViewLight)
        CardViewLight.setOnClickListener {
            if (!LightCoverShowing){
                LightCoverShowing = true
                val ButtonSetLightFrame = findViewById<FrameLayout>(R.id.buttonSetLightFrame)
                ButtonSetLightFrame.visibility = View.VISIBLE
                val sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
                if (sharedPreferences.contains("is_light_wallpaper_set?")) {
                    val ButtonSetLight = findViewById<TextView>(R.id.buttonSetLight)
                    ButtonSetLight.text = "立即设置壁纸"
                }else{
                    val ButtonSetLight = findViewById<TextView>(R.id.buttonSetLight)
                    ButtonSetLight.text = "立即选择图片"
                }
            }else{
                LightCoverShowing = false
                val ButtonSetLightFrame = findViewById<FrameLayout>(R.id.buttonSetLightFrame)
                ButtonSetLightFrame.visibility = View.GONE
            }

        }
        val ButtonSetLight = findViewById<TextView>(R.id.buttonSetLight)
        ButtonSetLight.setOnClickListener {
            if (ButtonSetLight.text == "立即选择图片") {
                mode_pictureSelect = "light"
                openGallery()
            }else{
                switchNow("light")
            }
        }


    }//onCreate END

    override fun onResume() {
        super.onResume()
        val ButtonSetLightFrame = findViewById<FrameLayout>(R.id.buttonSetLightFrame)
        val ButtonDarkLightFrame = findViewById<FrameLayout>(R.id.buttonSetDarkFrame)
        ButtonSetLightFrame.visibility = View.GONE
        ButtonDarkLightFrame.visibility = View.GONE
    }

    //Functions
    private fun saveSwitchState(key: String, isChecked: Boolean) {
        val sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
        sharedPreferences.edit {
            putBoolean(key, isChecked)
            apply()
        }
    }

    private fun restoreSwitchState(key: String) {
        val sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val isChecked = sharedPreferences.getBoolean(key, false)
        if (key == "saveToPublic") {
            Switch1.isChecked = isChecked
        }
        if (key == "slightmove") {
            Switch2.isChecked = isChecked
        }
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                val selectedImageUri = result.data?.data
                selectedImageUri?.let { saveImage(it) }
            }
        }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

    private fun saveImage(uri: Uri) {
        fun cropBitmap(bitmapScaled: Bitmap, x: Int, y: Int, width: Int, height: Int): Bitmap {
            return Bitmap.createBitmap(bitmapScaled, x, y, width, height)
        }
        fun info(bitmapForScale: Bitmap): Bitmap {
            //获取图片分辨率
            val picWidth = bitmapForScale.width
            val picHeight = bitmapForScale.height
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
            //缩放：可以把任何图片缩放并裁剪为手机分辨率
            var bitmapScaled: Bitmap
            val heightRatio = screenHeight.toFloat() / picHeight.toFloat()
            val newWidth = (picWidth * heightRatio).toInt()
            val newHeight = (picHeight * heightRatio).toInt()
            bitmapScaled = bitmapForScale.scale(newWidth, newHeight)
            val picWidth2= bitmapScaled.width
            if (picWidth2>=screenWidth){
                var x: Int
                x= (picWidth2/2) - (screenWidth/2)
                val y=0
                val width=screenWidth
                val height=screenHeight
                bitmapScaled=cropBitmap(bitmapScaled,x,y,width,height)
            }
            if(picWidth2<screenWidth){
                val widthRatio = screenWidth.toFloat() / picWidth.toFloat()
                val newWidth = (picWidth * widthRatio).toInt()
                val newHeight = (picHeight * widthRatio).toInt()
                bitmapScaled = bitmapForScale.scale(newWidth, newHeight)
                val picHeight2= bitmapScaled.height
                val x = 0
                var y: Int
                y= (picHeight2/2) - (screenHeight/2)
                val width=screenWidth
                val height=screenHeight
                bitmapScaled=cropBitmap(bitmapScaled,x,y,width,height)
            }
            //微动
            val sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
            if(Switch2.isChecked) {
                if(sharedPreferences.contains("SlightValue")){
                    val number = 0
                    val x=0
                    val y=sharedPreferences.getInt("SlightValue",number)
                    val width=screenWidth
                    val height=screenHeight-2*y
                    bitmapScaled=cropBitmap(bitmapScaled,x,y,width,height)
                } else {
                    val x=0
                    val y=50
                    val width=screenWidth
                    val height=screenHeight-100
                    bitmapScaled=cropBitmap(bitmapScaled,x,y,width,height)
                }
            }
            return bitmapScaled
        }
        contentResolver.openInputStream(uri)?.use { inputStream ->
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val bitmapForScale = bitmap.copy(Bitmap.Config.ARGB_8888, true)
            val croppedBitmap = info(bitmapForScale)
            val sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
            val whatMode = mode_pictureSelect
            val fileName = when (whatMode) {
                "dark" -> "selected_image_dark.jpg"
                "light" -> "selected_image_light.jpg"
                else -> "selected_image_default.jpg"
            }
            val fileDir = File(filesDir, "images")
            if (!fileDir.exists()) {
                fileDir.mkdirs()
            }
            val file = File(fileDir, fileName)
            FileOutputStream(file).use { outputStream ->
                croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            }
            if (Switch1.isChecked) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/使用过的壁纸")
                }
                val imageUri = contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentValues
                )
                imageUri?.let {
                    val outputStream: OutputStream? = contentResolver.openOutputStream(it)
                    outputStream?.use { stream ->
                        croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                    }
                }
            }
            //写入参数：是否已设置
            when (whatMode) {
                "dark" -> {
                    sharedPreferences.edit {
                        putBoolean("is_dark_wallpaper_set?", true)
                        apply()
                    }
                    loadImage()
                }
                "light" -> {
                    sharedPreferences.edit {
                        putBoolean("is_light_wallpaper_set?", true)
                        apply()
                    }
                    loadImage()
                }
            }
        }
    }

    @SuppressLint("CutPasteId")
    private fun loadImage() {
        val sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
        if (sharedPreferences.contains("is_dark_wallpaper_set?") && (!sharedPreferences.contains("Clear"))) {
            val fileName = "selected_image_dark.jpg"
            val fileDir = File(filesDir, "images")
            val file = File(fileDir, fileName)
            if (file.exists()) {
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                val imageView = findViewById<ImageView>(R.id.imageDark)
                imageView.setImageBitmap(bitmap)
            }
        }
        if (sharedPreferences.contains("is_light_wallpaper_set?") && (!sharedPreferences.contains("Clear"))) {
            val fileName = "selected_image_light.jpg"
            val fileDir = File(filesDir, "images")
            val file = File(fileDir, fileName)

            if (file.exists()) {
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                val imageView = findViewById<ImageView>(R.id.imageLight)
                imageView.setImageBitmap(bitmap)
            }
        }
        if (sharedPreferences.contains("Clear")) {
            val imageView1 = findViewById<ImageView>(R.id.imageLight)
            val bitmap1 = BitmapFactory.decodeResource(resources, R.drawable.ic_notset)
            imageView1.setImageBitmap(bitmap1)

            val imageView2 = findViewById<ImageView>(R.id.imageDark)
            val bitmap2 = BitmapFactory.decodeResource(resources, R.drawable.ic_notset)
            imageView2.setImageBitmap(bitmap2)
            sharedPreferences.edit {
                remove("Clear")
                apply()
            }
        }

    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun preCheck() {
        //准备工作1：申请储存
        //无需申请此权限，使用媒体库api来保存图片到外部即可！

        //准备工作2：检查壁纸是否设置，动态切换样式
        val sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
        if (sharedPreferences.contains("is_dark_wallpaper_set?")) {
            loadImage()
        }

        if (sharedPreferences.contains("is_light_mode_set?")) {
            loadImage()
        }

        if (!sharedPreferences.contains("whatmode?")) {
            sharedPreferences.edit {
                putString("whatmode?", "")
                apply()
            }
        }
        //准备工作2.5:状态重置
        if (sharedPreferences.contains("running")) {
            sharedPreferences.edit {
                remove("running")
                apply()
            }
        }
        //准备工作3：读取设置
        val showGuidance = sharedPreferences.getInt("showGuidance", 0)
        val showGuidanceAnimation = sharedPreferences.getInt("showGuidanceAnimation", 0)
        killWhenExit = sharedPreferences.getInt("killWhenExit", 0)

        //后级工作：动态切换文字
        if (!sharedPreferences.contains("is_dark_wallpaper_set?")) {
            val buttonChangeDark = findViewById<TextView>(R.id.buttonChangeDark)
            buttonChangeDark.text = "选择"

        }
        if (!sharedPreferences.contains("is_light_wallpaper_set?")) {
            val buttonChangeLight = findViewById<TextView>(R.id.buttonChangeLight)
            buttonChangeLight.text = "选择"
        }
        //Description
        if (showGuidance == 1) {
            val composeView = findViewById<ComposeView>(R.id.compose1)
            composeView.setContent {
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
                    enter = slideInVertically(
                        initialOffsetY = { Y },
                        animationSpec = tween(durationMillis = T)
                    )
                ){
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                            .background(colorResource(id = R.color.HeadBackground))
                            .border(
                                1.dp,
                                colorResource(id = R.color.HeadText),
                                RoundedCornerShape(16.dp)
                            )
                            .padding(10.dp)

                    ) {
                        Text(
                            text = getString(R.string.description_darkmode),
                            style = TextStyle(fontSize = 14.sp),
                            color = colorResource(id = R.color.HeadText),
                        )
                    }
                }
            }
        }



    }

    private fun clearWallPaper() {
        val sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
        sharedPreferences.edit {
            remove("is_dark_wallpaper_set?")
            remove("is_light_wallpaper_set?")
            putString("Clear", "")
            apply()
            loadImage()
        }
        val ButtonSetLightFrame = findViewById<FrameLayout>(R.id.buttonSetLightFrame)
        val ButtonDarkLightFrame = findViewById<FrameLayout>(R.id.buttonSetDarkFrame)
        ButtonSetLightFrame.visibility = View.GONE
        ButtonDarkLightFrame.visibility = View.GONE
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun switchNow(mode: String) {
        if (mode == "dark") {
            findViewById<Button>(R.id.buttonSwitchDark).text = "请等待应用自行退出，避免卡顿"
            findViewById<Button>(R.id.buttonSwitchDark).setBackgroundColor(ContextCompat.getColor(this,R.color.ButtonBgHighlight))
            val sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
            if (sharedPreferences.contains("running")) {
                runOnUiThread {
                    findViewById<Button>(R.id.buttonSwitchDark).text = "请勿重复点击,否则易导致界面卡死!"
                    findViewById<Button>(R.id.buttonSwitchDark).setBackgroundColor(ContextCompat.getColor(this,R.color.ButtonBgVeryHeavy))
                }
                return
            }

            GlobalScope.launch {
                sharedPreferences.edit {
                    putString("running", "1")
                    apply()
                }
                if (sharedPreferences.contains("is_dark_wallpaper_set?")) {
                    val fileName = "selected_image_dark.jpg"
                    val fileDir = File(filesDir, "images")
                    val file = File(fileDir, fileName)
                    if (file.exists()) {
                        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                        val wallpaperManager = WallpaperManager.getInstance(this@DarkModeActivity)
                        wallpaperManager.setBitmap(
                            bitmap, null, false,
                            WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK
                        )
                    }
                    val intent = Intent(Intent.ACTION_MAIN)
                    intent.addCategory(Intent.CATEGORY_HOME)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    sharedPreferences.edit {
                        remove("running")
                        commit()
                    }
                    runOnUiThread {
                        finish()
                    }
                    GlobalScope.launch(Dispatchers.IO) {
                        exit(1000)
                    }
                } else {
                    runOnUiThread {
                        findViewById<Button>(R.id.buttonSwitchDark).text = "您未设置深色模式壁纸"
                    }
                    sharedPreferences.edit {
                        remove("running")
                        apply()
                    }
                }
            }
            return
        }
        if (mode == "light") {
            findViewById<Button>(R.id.buttonSwitchLight).text = "请等待应用自行退出，避免卡顿"
            findViewById<Button>(R.id.buttonSwitchLight).setBackgroundColor(ContextCompat.getColor(this,R.color.ButtonBgHighlight))

            val sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
            if (sharedPreferences.contains("running")) {
                runOnUiThread {
                    findViewById<Button>(R.id.buttonSwitchLight).text = "请勿重复点击,否则易导致界面卡死!"
                    findViewById<Button>(R.id.buttonSwitchLight).setBackgroundColor(ContextCompat.getColor(this,R.color.ButtonBgVeryHeavy))
                }
                return
            }

            GlobalScope.launch(Dispatchers.IO) {
                sharedPreferences.edit {
                    putString("running", "1")
                    apply()
                }
                val sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
                if (sharedPreferences.contains("is_light_wallpaper_set?")) {
                    val fileName = "selected_image_light.jpg"
                    val fileDir = File(filesDir, "images")
                    val file = File(fileDir, fileName)
                    if (file.exists()) {
                        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                        val wallpaperManager = WallpaperManager.getInstance(this@DarkModeActivity)
                        wallpaperManager.setBitmap(
                            bitmap, null, false,
                            WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK
                        )
                    }
                    val intent = Intent(Intent.ACTION_MAIN)
                    intent.addCategory(Intent.CATEGORY_HOME)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    sharedPreferences.edit {
                        remove("running")
                        commit()
                    }
                    runOnUiThread {
                        finish()
                    }
                    GlobalScope.launch(Dispatchers.IO) {
                        exit(1000)
                    }

                } else {
                    runOnUiThread {
                        findViewById<Button>(R.id.buttonSwitchLight).setText("您未设置浅色模式壁纸")
                    }
                    sharedPreferences.edit {
                        remove("running")
                        apply()
                    }
                }
            }
            return
        }
    }

    private fun setValue(content:String){
        if (content.isEmpty()){
            notice("未填写有效内容",2000)
            return
        }
        val number = content.toInt()
        if(number<=200){
            val sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
            sharedPreferences.edit{
                putInt("SlightValue",number)
            }
            notice("已设置为$number",2000)
            return
        }
        else{
            notice("数值过大",2000)
            return
        }
    }

    private fun createShortcut(){
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

    @OptIn(DelicateCoroutinesApi::class)
    private fun exit(time:Long){
        GlobalScope.launch {
            delay(time)
            if (killWhenExit==1){
                finishAndRemoveTask()
                val pid = Process.myPid()
                Process.killProcess(pid)
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




