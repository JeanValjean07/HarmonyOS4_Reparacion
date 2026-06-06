package com.suming.reparacion.FunctionalPack

import android.content.Context
import java.io.File

class WallpaperFileWrapper {

    //包装文件
    private val name_dark_img = "dark_wallpaper_img.jpg"
    private val name_light_img = "light_wallpaper_img.jpg"
    private val name_error_img = "error_wallpaper_img.jpg"
    private val img_directory = "darkmode/current_wallpaper"

    fun wrapFile(context: Context, dir: String = "", name: String = "", mode: String = ""): File {
        //先保证路径存在
        fun makeSureDirExist(){
            val fileDir = File(context.filesDir, img_directory)
            if (!fileDir.exists()) {
                fileDir.mkdirs()
            }
        }
        makeSureDirExist()
        //根据传入参数返回对应文件
        if(dir != "" && name != ""){
            return File(context.filesDir.resolve(dir),name)
        }else{
            if(mode == "dark"){
                val file = File(context.filesDir.resolve(img_directory),name_dark_img)
                return file
            }else if(mode == "light"){
                val file = File(context.filesDir.resolve(img_directory),name_light_img)
                return file
            }else{
                val file = File(context.filesDir.resolve(img_directory),name_error_img)
                return file
            }
        }
    }

    fun getImageDir(): String {
        return img_directory
    }

}