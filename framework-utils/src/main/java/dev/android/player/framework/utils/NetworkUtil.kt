package dev.android.player.framework.utils

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager

/**
 *  description :
 */
object NetworkUtil {

    /**
     * 检测网络是否连接
     */
    @SuppressLint("MissingPermission")
    fun isNetworkConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            // 在 Android 6.0 及以上版本使用 NetworkCapabilities
//            val networkCapabilities = connectivityManager.activeNetwork ?: return false
//            val actNw = connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
//            return actNw.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
//        } else {
            // 在 Android 6.0 以下版本使用 isActiveNetworkConnected 方法
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            return activeNetworkInfo != null && activeNetworkInfo.isConnected
//        }
    }

}