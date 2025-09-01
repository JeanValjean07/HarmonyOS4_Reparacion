package com.suming.cpa

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

open class Receiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("Receiver", "Received broadcast: ${intent.action}")
    }


}
