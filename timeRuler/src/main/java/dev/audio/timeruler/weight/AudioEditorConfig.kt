package dev.audio.timeruler.weight

class AudioEditorConfig private constructor(
    @BaseAudioEditorView.Mode
    val mode: Int,
    val startValue: Long,
    val endValue: Long,
    val maxScreenSpanValue:Long,//一屏最大显示时间 一首歌曲的时间
) {
    class Builder(
        private var mode: Int = BaseAudioEditorView.MODE_ARRAY[2],
        private var startValue: Long = 0,
        private var endValue: Long = 0,
        private var maxScreenSpanValue: Long = BaseAudioEditorView.SCREEN_WIDTH_TIME_VALUE_ARRAY[5],
    ) {
        fun mode(mode: Int) = apply { this.mode = mode }
        fun startValue(startValue: Long) = apply { this.startValue = startValue }
        fun endValue(endValue: Long) = apply { this.endValue = endValue }
        fun maxScreenSpanValue(maxScreenSpanValue: Long) = apply { this.maxScreenSpanValue = maxScreenSpanValue }
        fun build() = AudioEditorConfig(mode, startValue, endValue,maxScreenSpanValue)
    }
}