package dev.audio.ffmpeglib

import android.app.Application

object FFmpegApplication {

    var instance: Application? = null

    fun onCreate(application: Application) {
        instance = application
    }

}
