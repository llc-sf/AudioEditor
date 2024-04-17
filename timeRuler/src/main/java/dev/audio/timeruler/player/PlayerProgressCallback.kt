package dev.audio.timeruler.player

interface PlayerProgressCallback {
    fun onProgressChanged(currentWindowIndex:Int,position: Long, duration: Long) // 添加这行代码
}