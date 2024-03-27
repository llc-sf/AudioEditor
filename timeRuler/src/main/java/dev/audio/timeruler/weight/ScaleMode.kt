package dev.audio.timeruler.weight

class ScaleMode {
    var startValue: Long = 0
    var endValue: Long = 0

    /**
     * 间隔多少毫秒是一个刻度（普通）
     */
    var unitValue: Long = 0

    /**
     * 间隔多少毫秒是一个关键刻度
     */
    var keyScaleRange: Long = 0
}