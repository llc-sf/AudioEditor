package dev.android.player.framework.utils

/**
 * 埋点工具
 */
object TrackerMultiple {

    private var mListener: OnTrackerEventListener? = null

    @JvmStatic
    fun setListener(listener: OnTrackerEventListener?) {
        this.mListener = listener
    }

    @JvmStatic
    fun onPagerPv(name: String) {
        mListener?.onEvent("PV", name)
    }

    @JvmStatic
    fun onEvent(event: String, value: String) {
        mListener?.onEvent(event, value)
    }

    @JvmStatic
    fun onEvent(event: String, extra: Map<String, Any>?) {
        mListener?.onEvent(event, extra)
    }

    @JvmStatic
    fun onException(exception: Throwable) {
        mListener?.onException(exception)
    }

    @JvmStatic
    fun onRecordLog(log: String) {
        mListener?.onRecordLog(log)
    }


    interface OnTrackerEventListener {

        fun onEvent(event: String, value: String)

        fun onEvent(event: String, extra: Map<String, Any>?)

        /**
         * 异常上报
         */
        fun onException(exception: Throwable)

        /**
         * 记录日志
         */
        fun onRecordLog(msg: String)
    }

}