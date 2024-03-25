package dev.android.player.framework.base

import android.app.Activity
import android.view.View

/**
 * Activity ContentView Transform Provider
 */
object ContentViewTransformProvider {

    private var mProvider: IProvider? = null

    init {
        mProvider = DefaultProvider()
    }


    @JvmStatic
    fun register(provider: IProvider) {
        mProvider = provider
    }

    @JvmStatic
    fun unregister() {
        mProvider = null
    }

    @JvmStatic
    fun getContentViewView(activity: Activity, view: View?): View? {
        return mProvider?.getContentViewView(activity, view) ?: view
    }

    /**
     * 默认不进行任何变换
     */
    private class DefaultProvider : IProvider {
        override fun getContentViewView(activity: Activity, view: View?): View? {
            return view
        }
    }

    interface IProvider {
        /**
         * 对Activity的ContentView进行变换
         * @param activity Activity
         * @param view View 原始的ContentView
         * @return View 变换后的ContentView
         */
        fun getContentViewView(activity: Activity, view: View?): View?
    }

}

