package com.suming.reparacion.DataBase.Notification

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [NotificationSetting::class], version = 10, exportSchema = false)
abstract class NotificationDataBase : RoomDatabase() {
    abstract fun mediaItemDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: NotificationDataBase? = null

        fun get(context: Context): NotificationDataBase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    NotificationDataBase::class.java,
                    "MediaItemSetting.db"
                )
                    .fallbackToDestructiveMigration()
                    .build().also { INSTANCE = it }
            }
    }







}