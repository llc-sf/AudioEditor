package com.music.font

import android.graphics.Typeface
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import androidx.core.provider.FontRequest
import androidx.core.provider.FontsContractCompat
import com.android.app.AppProvider

object FontUtils {

    private val mLoaderThread by lazy {
        HandlerThread("FontLoader").apply {
            start()
        }
    }

    private val mLoaderHandler by lazy {
        Handler(mLoaderThread.looper)
    }


    fun init() { //Request Regular Font
        FontsContractCompat.requestFont(AppProvider.get(), PoppinsRequests.Black()
            .getRequests(), PoppinsRequestCallBack(Poppins.Poppins), mLoaderHandler)
    }

    /**
     * 通过字体名称获取字体
     */
    sealed class PoppinsRequests(val key: String, val query: String) {
        class Black : PoppinsRequests(Poppins.Poppins, "name=Poppins")

        fun getRequests(): FontRequest {
            return FontRequest("com.google.android.gms.fonts", "com.google.android.gms", query, R.array.com_google_android_gms_fonts_certs)
        }
    }


    /**
     * 字体请求回调，用于将字体缓存到内存中
     */
    private class PoppinsRequestCallBack(@Poppins.FontFamily val name: String) :
        FontsContractCompat.FontRequestCallback() {

        private val TAG = "PoppinsRequestCallBack"

        init {
            Log.d(TAG, "PoppinsRequestCallBack Begin Request, name=$name")
        }

        override fun onTypefaceRetrieved(typeface: Typeface) {
            Log.d(TAG, "onTypefaceRetrieved: Typeface = $name => $typeface")
            Poppins.putTypefaceToCache(name, typeface)
        }

        override fun onTypefaceRequestFailed(reason: Int) {
            Log.e(TAG, "onTypefaceRequestFailed: Typeface = $name => $reason")
        }
    }

}