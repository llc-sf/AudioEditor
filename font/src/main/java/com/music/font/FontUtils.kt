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
        FontsContractCompat.requestFont(AppProvider.get(), PoppinsRequests.Regular()
            .getRequests(), PoppinsRequestCallBack(Poppins.Poppins), mLoaderHandler)
        FontsContractCompat.requestFont(AppProvider.get(), PoppinsLightRequests.Light()
            .getRequests(), PoppinsLightRequestCallBack(PoppinsLight.PoppinsLight), mLoaderHandler)
        FontsContractCompat.requestFont(AppProvider.get(), PoppinsMediumRequests.Medium()
            .getRequests(), PoppinsMediumRequestCallBack(PoppinsMedium.PoppinsMedium), mLoaderHandler)
        FontsContractCompat.requestFont(AppProvider.get(), PoppinsSemiBoldRequests.SemiBold()
            .getRequests(), PoppinsSemiBoldRequestCallBack(PoppinsSemiBold.PoppinsSemiBold), mLoaderHandler)
        FontsContractCompat.requestFont(AppProvider.get(), PoppinsBoldRequests.Bold()
            .getRequests(), PoppinsBoldRequestCallBack(PoppinsBold.PoppinsBold), mLoaderHandler)
    }

    /**
     * 通过字体名称获取字体
     */
    sealed class PoppinsRequests(val key: String, val query: String) {
        class Regular : PoppinsRequests(Poppins.Poppins, "name=Poppins")

        fun getRequests(): FontRequest {
            return FontRequest("com.google.android.gms.fonts", "com.google.android.gms", query, R.array.com_google_android_gms_fonts_certs)
        }
    }

    sealed class PoppinsLightRequests(val key: String, val query: String) {
        class Light : PoppinsLightRequests(PoppinsLight.PoppinsLight, "name=Poppins&amp;weight=300")

        fun getRequests(): FontRequest {
            return FontRequest("com.google.android.gms.fonts", "com.google.android.gms", query, R.array.com_google_android_gms_fonts_certs)
        }
    }

    sealed class PoppinsMediumRequests(val key: String, val query: String) {
        class Medium : PoppinsMediumRequests(PoppinsMedium.PoppinsMedium, "name=Poppins&amp;weight=500")

        fun getRequests(): FontRequest {
            return FontRequest("com.google.android.gms.fonts", "com.google.android.gms", query, R.array.com_google_android_gms_fonts_certs)
        }
    }

    sealed class PoppinsSemiBoldRequests(val key: String, val query: String) {
        class SemiBold : PoppinsSemiBoldRequests(PoppinsSemiBold.PoppinsSemiBold, "name=Poppins&amp;weight=600")

        fun getRequests(): FontRequest {
            return FontRequest("com.google.android.gms.fonts", "com.google.android.gms", query, R.array.com_google_android_gms_fonts_certs)
        }
    }

    sealed class PoppinsBoldRequests(val key: String, val query: String) {
        class Bold : PoppinsBoldRequests(PoppinsBold.PoppinsBold, "name=Poppins&amp;weight=700")

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


    private class PoppinsLightRequestCallBack(@PoppinsLight.FontFamily val name: String) :
        FontsContractCompat.FontRequestCallback() {

        private val TAG = "PoppinsLightRCB"

        init {
            Log.d(TAG, "PoppinsLightRequestCallBack Begin Request, name=$name")
        }

        override fun onTypefaceRetrieved(typeface: Typeface) {
            Log.d(TAG, "onTypefaceRetrieved: Typeface = $name => $typeface")
            PoppinsLight.putTypefaceToCache(name, typeface)
        }

        override fun onTypefaceRequestFailed(reason: Int) {
            Log.e(TAG, "onTypefaceRequestFailed: Typeface = $name => $reason")
        }
    }

    private class PoppinsMediumRequestCallBack(@PoppinsMedium.FontFamily val name: String) :
        FontsContractCompat.FontRequestCallback() {

        private val TAG = "PoppinsMediumRCB"

        init {
            Log.d(TAG, "PoppinsMediumRequestCallBack Begin Request, name=$name")
        }

        override fun onTypefaceRetrieved(typeface: Typeface) {
            Log.d(TAG, "onTypefaceRetrieved: Typeface = $name => $typeface")
            PoppinsMedium.putTypefaceToCache(name, typeface)
        }

        override fun onTypefaceRequestFailed(reason: Int) {
            Log.e(TAG, "onTypefaceRequestFailed: Typeface = $name => $reason")
        }
    }

    private class PoppinsSemiBoldRequestCallBack(@PoppinsSemiBold.FontFamily val name: String) :
        FontsContractCompat.FontRequestCallback() {

        private val TAG = "PoppinsSemiBoldRCB"

        init {
            Log.d(TAG, "PoppinsSemiBoldRequestCallBack Begin Request, name=$name")
        }

        override fun onTypefaceRetrieved(typeface: Typeface) {
            Log.d(TAG, "onTypefaceRetrieved: Typeface = $name => $typeface")
            PoppinsSemiBold.putTypefaceToCache(name, typeface)
        }

        override fun onTypefaceRequestFailed(reason: Int) {
            Log.e(TAG, "onTypefaceRequestFailed: Typeface = $name => $reason")
        }
    }

    private class PoppinsBoldRequestCallBack(@PoppinsBold.FontFamily val name: String) :
        FontsContractCompat.FontRequestCallback() {

        private val TAG = "PoppinsBoldRCB"

        init {
            Log.d(TAG, "PoppinsBoldRequestCallBack Begin Request, name=$name")
        }

        override fun onTypefaceRetrieved(typeface: Typeface) {
            Log.d(TAG, "onTypefaceRetrieved: Typeface = $name => $typeface")
            PoppinsBold.putTypefaceToCache(name, typeface)
        }

        override fun onTypefaceRequestFailed(reason: Int) {
            Log.e(TAG, "onTypefaceRequestFailed: Typeface = $name => $reason")
        }
    }

}