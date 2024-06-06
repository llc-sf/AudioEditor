package com.san.audioeditor

import android.app.Application
import android.text.TextUtils
import androidx.appcompat.app.AppCompatDelegate
import com.music.font.FontUtils
import com.san.audioeditor.delete.DeleteSongAppListener
import com.san.audioeditor.setting.UIModeSettings
import dev.android.player.framework.base.ActivityStackManager
import dev.android.player.framework.utils.AndroidUtil
import dev.android.player.framework.utils.impl.ServiceCenter.registerService
import dev.android.player.framework.utils.service.AdService
import dev.android.player.framework.utils.service.AppService
import dev.android.player.framework.utils.service.PlayerService
import dev.audio.ffmpeglib.FFmpegApplication
import dev.audio.timeruler.imp.PlayerServiceImp
import dev.audio.timeruler.utils.AudioFileUtils


class AudioEditorApplication : Application() {

    init {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        UIModeSettings.setPreparedMode(AppCompatDelegate.MODE_NIGHT_YES);
    }

    companion object {
        private lateinit var mAppContext: AudioEditorApplication
        fun getAppContext(): AudioEditorApplication {
            return mAppContext
        }
    }

    override fun onCreate() {
        super.onCreate()
        mAppContext = this
        FFmpegApplication.onCreate(this)


        //播放服务死亡Mock
        val processName = AndroidUtil.getProcessName(this)
        val isMainProcess = TextUtils.equals(processName, packageName)
        if (isMainProcess) {
            initService()
            FontUtils.init() //删除歌曲
            registerActivityLifecycleCallbacks(DeleteSongAppListener()) //Activity 堆栈管理
            ActivityStackManager.getInstance().init(this)

            //清理临时文件
            AudioFileUtils.clearTempFiles()
        }
    }


    private fun initService() {
        registerService(PlayerService::class.java, PlayerServiceImp())
    }
}