package com.suming.cpa

import android.media.AudioAttributes
import android.media.AudioManager
import android.media.AudioTrack
import android.media.audiofx.DynamicsProcessing
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@Suppress("LocalVariableName")
class VolumeControl: AppCompatActivity() {




    private val audioManager by lazy { getSystemService(AUDIO_SERVICE) as AudioManager }
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()
        setContentView(R.layout.activity_volume_control)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_volume_control)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        //按钮：返回
        val ButtonExit = findViewById<ImageButton>(R.id.buttonExit)
        ButtonExit.setOnClickListener {
            finish()
        }
        //点击彩蛋
        val SignVolume = findViewById<ImageView>(R.id.sign_volume)
        SignVolume.setOnClickListener {
            val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
            val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, AudioManager.FLAG_SHOW_UI)
        }



        val dummy = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build())
            .setTransferMode(AudioTrack.MODE_STREAM)
            .setBufferSizeInBytes(1024)
            .build()
        dummy.play()
        val session = dummy.audioSessionId

        val dp = DynamicsProcessing.Config.Builder(
                DynamicsProcessing.VARIANT_FAVOR_FREQUENCY_RESOLUTION,
                10,                                                   // 2. channelCount
                true,                                                 // 3. preEqInUse
                4,                                                    // 4. preEqBandCount (PreEQ 启用 4 个频段)
                true,                                                // 5. mbcInUse
                1,                                                    // 6. mbcBandCount (MBC 不用，设为 1)
                true,                                                // 7. postEqInUse
                1,                                                    // 8. postEqBandCount (PostEQ 不用，设为 1)
                true
            ).build()

        val DynamicsProcesser = DynamicsProcessing(0, session, dp)
        DynamicsProcesser.enabled = true


        audioManager.setParameters("audio_session_id=$session")




        //按钮：读取通道数
        val readChannelCountButton = findViewById<Button>(R.id.readChannelCount)
        readChannelCountButton.setOnClickListener {
            val channelCount = DynamicsProcesser.channelCount
            Toast.makeText(this, "通道数: $channelCount", Toast.LENGTH_SHORT).show()
        }

        //按钮：修改增益
        val applyGainButton = findViewById<Button>(R.id.applyGain)
        applyGainButton.setOnClickListener {
            DynamicsProcesser.setInputGainbyChannel(0, -9f)
            DynamicsProcesser.setInputGainbyChannel(1, -9f)

        }

        //按钮：修改增益2
        val applyGain2Button = findViewById<Button>(R.id.applyGain2)
        applyGain2Button.setOnClickListener {
            DynamicsProcesser.setInputGainbyChannel(0, -15f)
            DynamicsProcesser.setInputGainbyChannel(1, -15f)

        }

        //按钮：降低
        val DecreaseButton = findViewById<Button>(R.id.Decrease)
        DecreaseButton.setOnClickListener {
            val gain0 = DynamicsProcesser.getInputGainByChannelIndex(0)
            val gain1 = DynamicsProcesser.getInputGainByChannelIndex(1)


            DynamicsProcesser.setInputGainbyChannel(0, gain0 - 1f)
            DynamicsProcesser.setInputGainbyChannel(1, gain1 - 1f)



        }

        //按钮：增加
        val IncreaseButton = findViewById<Button>(R.id.Increase)
        IncreaseButton.setOnClickListener {
            val gain0 = DynamicsProcesser.getInputGainByChannelIndex(0)
            val gain1 = DynamicsProcesser.getInputGainByChannelIndex(1)


            DynamicsProcesser.setInputGainbyChannel(0, gain0 + 1f)
            DynamicsProcesser.setInputGainbyChannel(1, gain1 + 1f)



        }
















    }
    private fun watchSessions() {
        scope.launch {
            while (isActive) {

                val sessions = audioManager.activePlaybackConfigurations



                    /*
                    .mapNotNull { it.audioSessionId }
                    .distinct()


                sessions.forEach { sessionId ->
                    val app = application as EqApplication
                    if (sessionId != 0 && !app.eqCache.containsKey(sessionId)) {
                        createEq(sessionId)?.let { app.eqCache[sessionId] = it }
                    }
                }
                delay(1000)

                     */
            }
        }
    }


}