package com.suming.reparacion.FunctionalPack

import android.content.Context
import com.suming.reparacion.DataPack.AppInfo

class ApplicationAccountant(private val context: Context) {



    fun getApplicationList(): List<AppInfo> {

        val appInfoList = mutableListOf<AppInfo>()
        val packageManager = context.packageManager
        val packageInfo = packageManager.getInstalledPackages(0)



        for (item in packageInfo) {
            val appInfo = AppInfo(
                appName = item.applicationInfo?.loadLabel(packageManager).toString(),
                appPackageName = item.packageName,

                //应用名称每个汉字的首字母
                appNameChar = item.applicationInfo?.loadLabel(packageManager).toString().first()
                    .toString(),
            )
            appInfoList.add(appInfo)
        }
        return appInfoList
    }



}