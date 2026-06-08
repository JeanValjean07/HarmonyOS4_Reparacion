package com.suming.reparacion.DataBase.Notification

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface NotificationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(item: NotificationSetting)

    @Query("SELECT * FROM MediaItemSetting WHERE MARK_FileName = :path LIMIT 1")
    suspend operator fun get(path: String): NotificationSetting?



    @Query("UPDATE MediaItemSetting SET PREFS_AlwaysSeek = :newValue1 WHERE MARK_FileName = :videoId")
    suspend fun update_PREFS_AlwaysSeek(videoId: String,newValue1: Boolean)

    @Query("UPDATE MediaItemSetting SET PREFS_LinkScroll = :newValue1 WHERE MARK_FileName = :videoId")
    suspend fun update_PREFS_LinkScroll(videoId: String,newValue1: Boolean)

    @Query("UPDATE MediaItemSetting SET PREFS_TapJump = :newValue1 WHERE MARK_FileName = :videoId")
    suspend fun update_PREFS_TapJump(videoId: String,newValue1: Boolean)

    @Query("UPDATE MediaItemSetting SET PREFS_VideoOnly = :newValue1 WHERE MARK_FileName = :videoId")
    suspend fun update_PREFS_VideoOnly(videoId: String,newValue1: Boolean)
    @Query("SELECT PREFS_VideoOnly FROM MediaItemSetting WHERE MARK_FileName = :videoId LIMIT 1")
    suspend fun get_PREFS_VideoOnly(videoId: String): Boolean


    @Query("UPDATE MediaItemSetting SET PREFS_SoundOnly = :newValue1 WHERE MARK_FileName = :videoId")
    suspend fun update_PREFS_SoundOnly(videoId: String,newValue1: Boolean)
    @Query("SELECT PREFS_SoundOnly FROM MediaItemSetting WHERE MARK_FileName = :videoId LIMIT 1")
    suspend fun get_PREFS_SoundOnly(videoId: String): Boolean


    @Query("UPDATE MediaItemSetting SET PREFS_SavePositionWhenExit = :newValue WHERE MARK_FileName = :videoId")
    suspend fun update_PREFS_saveLastPosition(videoId: String,newValue: Boolean)
    @Query("SELECT PREFS_SavePositionWhenExit FROM MediaItemSetting WHERE MARK_FileName = :videoId LIMIT 1")
    suspend fun get_PREFS_saveLastPosition(videoId: String): Boolean
    @Query("UPDATE MediaItemSetting SET SaveState_ExitPosition = :newValue WHERE MARK_FileName = :videoId")
    suspend fun update_value_LastPosition(videoId: String,newValue: Long)
    @Query("SELECT SaveState_ExitPosition FROM MediaItemSetting WHERE MARK_FileName = :videoId LIMIT 1")
    suspend fun get_value_LastPosition(videoId: String): Long



    @Query("UPDATE MediaItemSetting SET SavePath_Cover = :newValue WHERE MARK_FileName = :videoId")
    suspend fun update_cover_path(videoId: String,newValue: String)


    //快速预写
    suspend fun preset_all_row(MARK_FileName: String, PREFS_BackgroundPlay: Boolean, PREFS_LoopPlay: Boolean, PREFS_TapJump : Boolean, PREFS_LinkScroll : Boolean, PREFS_AlwaysSeek : Boolean,
        PREFS_VideoOnly: Boolean, PREFS_SoundOnly: Boolean, PREFS_PlaySpeed: Float, PREFS_SavePositionWhenExit: Boolean, SaveState_ExitPosition: Long,
        SavePath_Cover: String, PREFS_Hide: Boolean){
        insertOrUpdate(
            NotificationSetting(
                MARK_FileName = MARK_FileName,
                PREFS_BackgroundPlay = PREFS_BackgroundPlay,
                PREFS_LoopPlay = PREFS_LoopPlay,
                PREFS_TapJump = PREFS_TapJump,
                PREFS_LinkScroll = PREFS_LinkScroll,
                PREFS_AlwaysSeek = PREFS_AlwaysSeek,
                PREFS_VideoOnly = PREFS_VideoOnly,
                PREFS_SoundOnly = PREFS_SoundOnly,
                PREFS_PlaySpeed = PREFS_PlaySpeed,
                PREFS_SavePositionWhenExit = PREFS_SavePositionWhenExit,
                SaveState_ExitPosition = SaveState_ExitPosition,
                SavePath_Cover = SavePath_Cover,
                PREFS_Hide = PREFS_Hide
            )
        )
    }


    //快速读取
    @Query("SELECT SavePath_Cover FROM MediaItemSetting WHERE MARK_FileName = :filename LIMIT 1")
    suspend fun get_saved_cover_path(filename: String): String?

    @Query("SELECT PREFS_Hide FROM MediaItemSetting WHERE MARK_FileName = :filename LIMIT 1")
    suspend fun get_saved_PREFS_Hide(filename: String): Boolean?




    @Delete
    suspend fun delete(item: NotificationSetting)
}