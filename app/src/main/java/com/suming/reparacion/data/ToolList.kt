package com.suming.reparacion.data

object ToolList {

    //工具列表
    val toolsList = listOf(
        ToolPackage(
            id = 1,
            name = "深色模式壁纸",
            description = "为深色模式设置独立壁纸并便捷切换",
            intent = "MANAGER_INTENT_DARK_MODE_WALLPAPER_SWITCH",

        ),
        ToolPackage(
            id = 2,
            name = "通知管理",
            description = "在华为设备上进入原生通知管理界面",
            intent = "MANAGER_INTENT_NOTIFICATION_MANAGER",

        ),
        ToolPackage(
            id = 3,
            name = "音量控制",
            description = "控制响度以实现细化音量调节",
            intent = "MANAGER_INTENT_VOLUME_CONTROL",

            ),
        ToolPackage(
            id = 4,
            name = "屏幕手电",
            description = "使用屏幕作为手电灯并快捷调节亮度",
            intent = "MANAGER_INTENT_SCREEN_FLASHLIGHT",

            ),
        ToolPackage(
            id = 5,
            name = "屏幕时钟",
            description = "全屏幕显示时钟",
            intent = "MANAGER_INTENT_SCREEN_CLOCK_SWITCH",

            ),
        ToolPackage(
            id = 6,
            name = "桌面时钟磁贴",
            description = "替换系统自带的时钟磁贴样式",
            intent = "MANAGER_INTENT_SCREEN_CLOCK_SWITCH",

            ),
        ToolPackage(
            id = 7,
            name = "暂定",
            description = "暂未实现",
            intent = "MANAGER_INTENT_NONE",

            ),






        )
}