package com.suming.reparacion.ActivityComponents.DarkMode

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.suming.reparacion.AddonTools.showCustomToast
import com.suming.reparacion.R
import com.suming.reparacion.SettingsRequestCenter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DarkModeFragment: DialogFragment() {
    companion object {
        fun newInstance(): DarkModeFragment = DarkModeFragment().apply { arguments = bundleOf(  ) }
    }


    @Suppress("DEPRECATION")
    override fun onStart() {
        super.onStart()
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE){
            //横屏时隐藏状态栏
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                ViewCompat.setOnApplyWindowInsetsListener(dialog?.window?.decorView ?: return) { _, _ -> WindowInsetsCompat.CONSUMED }
                //
                dialog?.window?.decorView?.post { dialog?.window?.insetsController?.let { controller ->
                    controller.hide(WindowInsets.Type.statusBars())
                    controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                } }
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
            //
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
        return inflater.inflate(R.layout.fragment_general, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //集中初始化
        init(view)

        //
        value_slightMove = SettingsRequestCenter.get_VALUE_SlightMove(requireContext())

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

    private fun init(view: View){
        //初始化composeRoot
        ComposeRoot = view.findViewById(R.id.fragment_compose_root)
        //设置卡片高度
        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT){
            ComposeRoot.layoutParams.height = (resources.displayMetrics.heightPixels * 0.8).toInt()
        }
    }




    @Composable
    fun ComposeRoot() {
        //在root中取颜色模式
        isDarkMode = isSystemInDarkTheme()
        ColorPack = if (isDarkMode) DarkColorScheme else LightColorScheme
        //使用Box作为根布局
        Box(modifier = Modifier
            .fillMaxSize()
            .background(ColorPack.surface)
        ) {
            //顶部栏高度值
            var topBarHeight by remember { mutableIntStateOf(300) }
            val topPaddingDp = with(LocalDensity.current) {
                topBarHeight.toDp()
            }


            //最底层


            //内容层
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
                .height(60.dp)
                .onGloballyPositioned { coordinates ->
                    onHeightMeasured(coordinates.size.height)
                },
            color = Color.Transparent,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
            ){
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
                            text = "设置与更多选项",
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
    fun CapsuleButton(onClick: () -> Unit,
                      modifier: Modifier = Modifier,
                      text: String,
                      backgroundColor: Color = ColorPack.background,
                      border: BorderStroke = BorderStroke(
                          width = 0.5.dp,
                          color = Color.Companion.Gray.copy(alpha = 0.1f)
                      ),
                      elevation: Dp = 2.dp,
                      enabled: Boolean = true,
                      horizontalPadding: Dp = 10.dp,
                      verticalPadding: Dp = 5.dp,
                      textColor: Color = ColorPack.secondary) {
        val backgroundModifier = Modifier.background(backgroundColor)
        Box(
            modifier = modifier
                .wrapContentWidth()
                .height(35.dp)
                .shadow(
                    elevation = elevation,
                    shape = CircleShape,
                    clip = false,
                    spotColor = Color.Black.copy(alpha = 0.4f),
                    ambientColor = Color.Black.copy(alpha = 0.4f)
                )
                .clip(CircleShape)
                .then(backgroundModifier)
                .then(Modifier.border(border, CircleShape))
                .clickable(
                    enabled = enabled,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(
                        bounded = true,
                        color = Color.Gray
                    )
                ) { onClick() }
                .padding(horizontal = horizontalPadding, vertical = verticalPadding)
        ) {
            Row(
                modifier = Modifier.fillMaxHeight(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = text,
                    fontSize = 12.sp,
                    color = textColor,
                )
            }
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
    fun ContentRoot(topBarHeight: Dp) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = topBarHeight),
        ) {
            Settings()
        }
    }
    @Composable
    fun Settings(){
        //读取设置项
        //设置项1 - 保存到外部储存
        val flag_saveToExternal = remember { mutableStateOf(false) }
        fun updateLocalPrefRemember_flag_saveToExternal (){
            flag_saveToExternal.value = SettingsRequestCenter.get_PREFS_Save_Clip_Out(requireContext())
        }
        //设置项2 - 启用微动效果
        val flag_enableSlightMove = remember { mutableStateOf(false) }
        fun updateLocalPrefRemember_flag_enableSlightMove(){
            flag_enableSlightMove.value = SettingsRequestCenter.get_PREFS_SlightMove_Clip(requireContext())
        }
        //设置项3 - 退出后结束进程
        val flag_endProcess = remember { mutableStateOf(false) }
        fun updateLocalPrefRemember_flag_endProcess(){
            flag_endProcess.value = SettingsRequestCenter.get_PREFS_End_Process_After(requireContext())
        }


        //集中读取设置
        LaunchedEffect(Unit) {
            flag_saveToExternal.value = SettingsRequestCenter.get_PREFS_Save_Clip_Out(requireContext())
            flag_enableSlightMove.value = SettingsRequestCenter.get_PREFS_SlightMove_Clip(requireContext())
            flag_endProcess.value = SettingsRequestCenter.get_PREFS_End_Process_After(requireContext())
        }


        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 3.dp)
                .uniformShadow()
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                .background(Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(15.dp),
            border = BorderStroke(
                width = 0.5.dp,
                color = Color.Gray.copy(alpha = 0.1f)
            ),
            colors = CardDefaults.cardColors(containerColor = ColorPack.background)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "将裁剪后的图片保存到外部储存",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorPack.primary,
                            modifier = Modifier.padding(start = 0.dp)
                        )
                        Spacer(modifier = Modifier.padding(top = 2.dp))
                        Text(
                            text = "收集没有标题和内容文本的通知",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Normal,
                            color = ColorPack.secondary,
                            modifier = Modifier.padding(start = 0.dp)
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Switch(
                            checked = flag_saveToExternal.value,
                            onCheckedChange = {
                                SettingsRequestCenter.set_PREFS_Save_Clip_Out(it)
                                updateLocalPrefRemember_flag_saveToExternal()
                            },
                            modifier = Modifier.padding(end = 10.dp)
                        )

                    }
                }
            }
        }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 3.dp)
                .uniformShadow()
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                .background(Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(15.dp),
            border = BorderStroke(
                width = 0.5.dp,
                color = Color.Gray.copy(alpha = 0.1f)
            ),
            colors = CardDefaults.cardColors(containerColor = ColorPack.background)
        ){
            Column(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "启用微动效果",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorPack.primary,
                            modifier = Modifier.padding(start = 0.dp)
                        )
                        Spacer(modifier = Modifier.padding(top = 2.dp))
                        Text(
                            text = "允许可清除的通知被隐藏",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Normal,
                            color = ColorPack.secondary,
                            modifier = Modifier.padding(start = 0.dp)
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Switch(
                            checked = flag_enableSlightMove.value,
                            onCheckedChange = {
                                SettingsRequestCenter.set_PREFS_SlightMove_Clip(it)
                                updateLocalPrefRemember_flag_enableSlightMove()
                            },
                            modifier = Modifier.padding(end = 10.dp)
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "微动量数值",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorPack.primary,
                            modifier = Modifier.padding(start = 0.dp)
                        )
                        Spacer(modifier = Modifier.padding(top = 2.dp))
                        Text(
                            text = "当前值：${value_slightMove}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Normal,
                            color = ColorPack.secondary,
                            modifier = Modifier.padding(start = 0.dp)
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CapsuleButton(
                            text = "修改",
                            onClick = {
                                startSetValue()
                            },
                            modifier = Modifier.padding(end = 10.dp)
                        )
                    }
                }
            }
        }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 3.dp)
                .uniformShadow()
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                .background(Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(15.dp),
            border = BorderStroke(
                width = 0.5.dp,
                color = Color.Gray.copy(alpha = 0.1f)
            ),
            colors = CardDefaults.cardColors(containerColor = ColorPack.background)
        ){
            Column(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "自动退出后结束进程",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorPack.primary,
                            modifier = Modifier.padding(start = 0.dp)
                        )
                        Spacer(modifier = Modifier.padding(top = 2.dp))
                        Text(
                            text = "未开始时，只是单纯地回到桌面",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Normal,
                            color = ColorPack.secondary,
                            modifier = Modifier.padding(start = 0.dp)
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Switch(
                            checked = flag_endProcess.value,
                            onCheckedChange = {
                                SettingsRequestCenter.set_PREFS_End_Process_After(it)
                                updateLocalPrefRemember_flag_endProcess()
                            },
                            modifier = Modifier.padding(end = 10.dp)
                        )
                    }
                }
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
        surface = Color(0xFF181818),
        //一级和二级文字
        primary = Color(0xFFFFFFFF),
        secondary = Color(0xFFF6F6F6),
        //卡片底色
        background = Color(0xFF121212),
    )

    //设置数值
    private var value_slightMove by mutableIntStateOf(0)
    fun updateLocalPrefRemember_value_slightMove() {
        value_slightMove = SettingsRequestCenter.get_VALUE_SlightMove(requireContext())
    }
    @SuppressLint("UseGetLayoutInflater", "InflateParams")
    private fun startSetValue(){
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.popup_value_set, null)
        val dialog = Dialog(requireContext())
        dialog.setContentView(dialogView)
        //
        val title = dialogView.findViewById<TextView>(R.id.popup_title)
        val description = dialogView.findViewById<TextView>(R.id.popup_description)
        val input: EditText = dialogView.findViewById(R.id.popup_input)
        title.text = "设置微动量数值"
        description.text = "壁纸上下被裁剪的像素高度"
        input.hint = "(单位:像素个数丨默认50最大200)"
        //
        val button: Button = dialogView.findViewById(R.id.popup_button)
        button.text = "确认"
        button.setOnClickListener {
            val userInput = input.text.toString()
            setValue(userInput)
            dialog.dismiss()
        }
        //
        dialog.show()
        val imm = requireContext().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        CoroutineScope(Dispatchers.Main).launch {
            delay(100)
            input.requestFocus()
            imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT)
        }

    }
    private fun setValue(content:String){
        if (content.isEmpty()){
            requireContext().showCustomToast("未填写有效内容")
            return
        }
        val number = content.toInt()
        if(number<=200){
            SettingsRequestCenter.set_VALUE_SlightMove(number)
            requireContext().showCustomToast("已设置为$number")
            updateLocalPrefRemember_value_slightMove()
            return
        }
        else{
            requireContext().showCustomToast("数值过大")
            return
        }
    }


    //自定义退出逻辑
    /*
    private var lockPage = false
    private fun customDismiss(){
        if (!lockPage) {
            Dismiss(false)
        }
    }

    private fun Dismiss(flag_need_vibrate: Boolean = true){
        if (flag_need_vibrate){ ToolVibrate().vibrate(requireContext()) }
        val result = bundleOf("KEY" to "Dismiss")
        setFragmentResult("FROM_FRAGMENT_MORE_BUTTON", result)
        dismiss()

    }

     */

}