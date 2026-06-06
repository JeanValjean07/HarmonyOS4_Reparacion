package com.suming.reparacion.ActivityComponents

interface ReceiverCallback {

    fun onNotificationReceived(packageName:String, title:String, content: String)


}