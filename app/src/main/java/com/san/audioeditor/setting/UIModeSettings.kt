package com.san.audioeditor.setting

import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.NightMode

/**
 * 黑色白色模式设置
 */
object UIModeSettings {

    @NightMode
    private var mUiMode = AppCompatDelegate.getDefaultNightMode()


    @JvmStatic
    fun setPreparedMode(@NightMode mode: Int) {
        mUiMode = mode
    }

    @JvmStatic
    fun setMode(ac: AppCompatActivity?, @NightMode mode: Int) {
        mUiMode = mode
        ac?.delegate?.localNightMode = mode
    }

    @JvmStatic
    fun getDelegateMode(): Int {
        return mUiMode
    }


    /**
     * 注意注意，这里的配置模式和[getDelegateMode]不一样
     * 这里获取的值是根据[AppCompatDelegate.MODE_NIGHT_NO]、[AppCompatDelegate.MODE_NIGHT_YES]、[AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM]、[AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY]来判断的
     * 转化为 Configuration.UI_MODE_NIGHT_NO、Configuration.UI_MODE_NIGHT_YES、Configuration.UI_MODE_NIGHT_UNDEFINED
     */
    @JvmStatic
    fun getConfigurationMode(): Int {
        return when (mUiMode) {
            AppCompatDelegate.MODE_NIGHT_NO -> Configuration.UI_MODE_NIGHT_NO
            AppCompatDelegate.MODE_NIGHT_YES -> Configuration.UI_MODE_NIGHT_YES
            else -> Configuration.UI_MODE_NIGHT_UNDEFINED
        }
    }
}