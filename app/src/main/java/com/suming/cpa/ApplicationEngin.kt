package com.suming.cpa

import android.content.Context

class ApplicationEngin(private val context: Context) {



    fun getApplicationList(): List<appInfo> {

        val appInfoList = mutableListOf<appInfo>()
        val packageManager = context.packageManager
        val packageInfo = packageManager.getInstalledPackages(0)



        for (item in packageInfo) {
            val appInfo = appInfo(
                appName = item.applicationInfo?.loadLabel(packageManager).toString(),
                appPackageName = item.packageName,

                //应用名称每个汉字的首字母
                appNameChar = item.applicationInfo?.loadLabel(packageManager).toString().first().toString(),
            )
            appInfoList.add(appInfo)
        }
        return appInfoList
    }



}


data class appInfo(
    val appName: String,
    val appPackageName: String,
    val appNameChar: String,
)

