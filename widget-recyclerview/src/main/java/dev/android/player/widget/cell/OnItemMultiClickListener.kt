package dev.android.player.widget.cell

interface OnItemMultiClickListener {
    /**
     * 通用点击事件
     * @param actionType 点击类型
     * @param pos        pos
     * @param ext        ext
     */
    fun onBaseItemMultiClick(actionType: Int, pos: Int = 0, ext: Any? = null)
}