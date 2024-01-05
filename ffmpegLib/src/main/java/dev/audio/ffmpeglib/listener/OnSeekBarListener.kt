package dev.audio.ffmpeglib.listener

interface OnSeekBarListener {
    fun onProgress(index: Int, progress: Int)
}