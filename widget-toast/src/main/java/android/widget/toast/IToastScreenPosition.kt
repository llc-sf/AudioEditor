package android.widget.toast

/**
 * 指定Toast显示位置
 */
interface IToastScreenPosition {
    /**
     *
     * @return 垂直方向上的位置比例
     *如：0.1f表示底部1/10处
     *   0.5f表示屏幕中间
     *   1f表示屏幕顶部
     */
    fun getPosition(): Float
}