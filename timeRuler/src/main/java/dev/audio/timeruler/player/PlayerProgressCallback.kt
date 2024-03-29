package dev.audio.timeruler.player

interface PlayerProgressCallback {
    fun onProgressChanged(position: Long, duration: Long) // 添加这行代码
}