package com.suming.reparacion.FunctionalPack

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import java.io.File

class BitmapLoader {

    //传入目标File
    @SuppressLint("CutPasteId")
    fun loadBitmap(mode: String,file: File): Pair<Boolean, Bitmap?> {
        when(mode){
            //深色壁纸
            "dark" -> {
                consoleLog("BitmapLoader: 开始加载深色壁纸")
                //直接取文件实例
                if (file.exists()) {
                    var bitmap: Bitmap?
                    try{
                        bitmap = BitmapFactory.decodeFile(file.absolutePath)

                        consoleLog("BitmapLoader: 加载深色壁纸成功")

                        return Pair(true,bitmap)
                    }catch (_: Exception){
                        consoleLog("BitmapLoader: 加载深色壁纸失败")
                        return Pair(false,null)
                    }
                }else{
                    consoleLog("BitmapLoader: 深色壁纸文件不存在。复盘：Dir:${file.parentFile},FileName:${file.name}")

                    return Pair(false,null)
                }
            }
            //浅色壁纸
            "light" -> {
                consoleLog("BitmapLoader: 开始加载浅色壁纸")
                //直接取文件实例
                if (file.exists()) {
                    var bitmap: Bitmap?
                    try{
                        bitmap = BitmapFactory.decodeFile(file.absolutePath)

                        consoleLog("BitmapLoader: 加载浅色壁纸成功")

                        return Pair(true,bitmap)
                    }catch (_: Exception){
                        consoleLog("BitmapLoader: 加载浅色壁纸失败")
                        return Pair(false,null)
                    }
                }else{
                    consoleLog("BitmapLoader: 浅色壁纸文件不存在。复盘：Dir:${file.parentFile},FileName:${file.name}")
                    return Pair(false,null)
                }
            }
            //其他错误传参
            else -> {
                consoleLog("BitmapLoader: 收到无效的参数,期望参数为\"dark\"或\"light\"")
                return Pair(false,null)
            }
        }
    }
    //未传入File根据mode自己取
    fun loadBitmap(context: Context, mode: String, needClipped: Boolean = false): Bitmap? {
        val wallpaperFileWrapper = WallpaperFileWrapper()
        val (file, fileClipped) = wallpaperFileWrapper.wrapFile(context,mode = mode)

        if(needClipped){
            val bitmap = loadBitmap(mode, fileClipped).second

            return bitmap
        }else{
            val bitmap = loadBitmap(mode, file).second

            return bitmap
        }

    }


    //统一日志控制
    private fun consoleLog(msg: String, mark: Boolean = true) {
        if (mark) {
            Log.d("SuMing", msg)
        }
    }


}