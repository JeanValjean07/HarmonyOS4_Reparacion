package com.suming.reparacion.ActivityComponents;

interface ILocalNotificationManager {

     //取消通知
     void cancelNotificationByKey(String key);

     //获取所有通知
     StatusBarNotification[] getAllNotifications();


}