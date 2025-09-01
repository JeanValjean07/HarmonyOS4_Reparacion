package com.suming.cpa

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.cardview.widget.CardView
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class Settings : AppCompatActivity() {

    private lateinit var Switch0: SwitchCompat
    private lateinit var Switch1: SwitchCompat
    private lateinit var Switch2: SwitchCompat

    private var showGuidance = 1
    private var showGuidanceAnimation = 1
    private var killWhenExit = 0


    @SuppressLint("SetTextI18n", "InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_settings)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //开关：退出时自动结束进程
        Switch0 = findViewById(R.id.killWhenExit)
        Switch0.setOnCheckedChangeListener { _, isChecked ->
            saveSwitchState("killWhenExit", isChecked)
        }
        restoreSwitchState("killWhenExit")
        //开关：显示使用说明卡片
        Switch1 = findViewById(R.id.showGuidance)
        Switch1.setOnCheckedChangeListener { _, isChecked ->
            saveSwitchState("showGuidance", isChecked)
        }
        restoreSwitchState("showGuidance")
        //开关：显示使用说明卡片动画
        Switch2 = findViewById(R.id.showGuidanceAnimation)
        Switch2.setOnCheckedChangeListener { _, isChecked ->
            saveSwitchState("showGuidanceAnimation", isChecked)
        }
        restoreSwitchState("showGuidanceAnimation")
        //设置项读取
        val sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
        if (sharedPreferences.contains("SlightValue")){
            val number = sharedPreferences.getInt("SlightValue",100)
            val textViewSlightValue = findViewById<TextView>(R.id.TextViewSlightValue)
            textViewSlightValue.text = number.toString()
        } else{
            val textViewSlightValue = findViewById<TextView>(R.id.TextViewSlightValue)
            textViewSlightValue.text = "50（默认值）"
        }


        //按钮：关于/更新
        val buttonAbout = findViewById<Button>(R.id.buttonAbout)
        buttonAbout.setOnClickListener {
            val intent = Intent(this, About::class.java)
            startActivity(intent)
        }
        //按钮：退出
        val buttonExit = findViewById<ImageButton>(R.id.buttonExit)
        buttonExit.setOnClickListener {
            finish()
        }
        //按钮：设置微动值
        val buttonSlightValue = findViewById<Button>(R.id.buttonSlightValue)
        buttonSlightValue.setOnClickListener {
            val dialog = Dialog(this)
            val dialogView = LayoutInflater.from(this).inflate(R.layout.activity_dark_mode_settings, null)
            dialog.setContentView(dialogView)
            val input: EditText = dialogView.findViewById(R.id.dialog_input)
            val button: Button = dialogView.findViewById(R.id.dialog_button)
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            button.setOnClickListener {
                val userInput = input.text.toString()
                setValue(userInput)
                dialog.dismiss()
            }
            dialog.show()
            CoroutineScope(Dispatchers.Main).launch {
                delay(100)
                input.requestFocus()
                imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT)
            }
        }






    }//onCreate END

    //Functions
    private fun setValue(content:String){
        if (content.isEmpty()){
            notice("未填写有效内容", 2000)
            return
        }
        val number = content.toInt()
        if(number<=200){
            val sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
            sharedPreferences.edit{
                putInt("SlightValue",number)
            }
            val textViewSlightValue = findViewById<TextView>(R.id.TextViewSlightValue)
            textViewSlightValue.text = number.toString()
            return
        }
        else{
            notice("数值过大", 2000)
            return
        }
    }

    private val guidancePrefs = mapOf(
        "showGuidance"          to ::showGuidance,
        "showGuidanceAnimation" to ::showGuidanceAnimation,
        "killWhenExit"          to ::killWhenExit
    )

    private fun saveSwitchState(key: String, isChecked: Boolean) {
        val prop = guidancePrefs[key] ?: return
        val value = if (isChecked) 1 else 0
        prop.set(value)

        getSharedPreferences("app_prefs", MODE_PRIVATE)
            .edit { putInt(key, value).apply() }
    }

    private fun restoreSwitchState(key: String) {
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        if (key == "showGuidance"){
            val showGuidance = sharedPreferences.getInt("showGuidance", 1)
            if (showGuidance == 1){
                Switch1.isChecked = true
            }
            else{
                Switch1.isChecked = false
            }
        }
        if (key == "showGuidanceAnimation"){
            showGuidanceAnimation = sharedPreferences.getInt("showGuidanceAnimation", 1)
            if (showGuidanceAnimation == 1){
                Switch2.isChecked = true
            }
            else{
                Switch2.isChecked = false
            }
        }
        if (key == "killWhenExit"){
            killWhenExit = sharedPreferences.getInt("killWhenExit", 0)
            if (killWhenExit == 1){
                Switch0.isChecked = true
            }
            else{
                Switch0.isChecked = false
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

}//class End
