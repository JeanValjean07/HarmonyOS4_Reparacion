package com.suming.reparacion.FunctionalPack

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap

class WallpaperSetor {


    private var wallpaperManager: WallpaperManager? = null


    fun applySystemWallpaper(bitmap: Bitmap, context: Context){
        wallpaperManager = WallpaperManager.getInstance(context)
        //执行设置(需要收集各系统执行情况,扩展方法自定义)


        wallpaperManager?.setBitmap(
            bitmap, null, false,
            WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK
        )


    }



}