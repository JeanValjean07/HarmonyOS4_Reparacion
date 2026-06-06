package com.suming.reparacion.DataPack

data class ToolPackage(
    //基本字段
    val id: Int,
    val name: String,
    val description: String,

    //意图字段 规范为 MANAGER_INTENT_工具名称
    val intent: String = "MANAGER_INTENT_NONE",



)