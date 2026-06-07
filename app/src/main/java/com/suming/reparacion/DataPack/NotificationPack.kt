package com.suming.reparacion.DataPack

data class NotificationPack(

    val uniqueID: String,
    val key: String,
    val packageName: String,
    val postTime: Long,
    val isOngoing: Boolean,
    val title: String,
    val text: String,


)
