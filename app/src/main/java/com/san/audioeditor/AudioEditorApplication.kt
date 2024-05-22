package com.san.audioeditor

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.music.font.FontUtils
import com.san.audioeditor.setting.UIModeSettings
import dev.audio.ffmpeglib.FFmpegApplication

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
        FontUtils.init()
    }
}