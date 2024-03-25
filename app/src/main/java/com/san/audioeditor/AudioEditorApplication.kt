package com.san.audioeditor

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.san.audioeditor.setting.UIModeSettings
import dev.audio.ffmpeglib.FFmpegApplication

class AudioEditorApplication : Application() {

    init {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        UIModeSettings.setPreparedMode(AppCompatDelegate.MODE_NIGHT_YES);
    }

    override fun onCreate() {
        super.onCreate()

        FFmpegApplication.onCreate(this)
    }
}