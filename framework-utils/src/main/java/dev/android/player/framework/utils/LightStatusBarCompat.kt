package dev.android.player.framework.utils

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Environment
import androidx.annotation.ColorInt
import android.view.View
import android.view.Window
import android.view.WindowManager

import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.Properties


object LightStatusBarCompat {

    private val IMPL: ILightStatusBar

    private val isSupportLightMode: Boolean
        get() = MIUILightStatusBarImpl.isMe || Build.VERSION.SDK_INT >= Build.VERSION_CODES.M || MeizuLightStatusBarImpl.isMe

    internal interface ILightStatusBar {
        fun setLightStatusBar(window: Window, lightStatusBar: Boolean)
    }

    init {
        IMPL = when {
            MIUILightStatusBarImpl.isMe -> MIUILightStatusBarImpl()
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> MLightStatusBarImpl()
            MeizuLightStatusBarImpl.isMe -> MeizuLightStatusBarImpl()
            else -> object : ILightStatusBar {
                override fun setLightStatusBar(window: Window, lightStatusBar: Boolean) {}
            }
        }
    }

    /**
     * 设置statusBar透明兼容4.4 / 5.x / 6.x
     * 适用于有图片为头部的页面
     *
     * @param activity flag_status：0表示取消，WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS 表示透明
     */
    fun setStatusBarTranslucentCompat(activity: Activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT)
            return

        val window = activity.window

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        } else {
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        }
    }

    /**
     * 获取状态栏高度
     *
     * @param context
     * @return
     */
    fun getStatusBarHeight(context: Context): Int {
        var result = 0
        val resourceId = context.resources.getIdentifier(
                "status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = context.resources.getDimensionPixelSize(resourceId)
        }
        return result
    }

    @JvmOverloads
    fun decorateFakeStatusBar(statusBar: View, actionBarLightMode: Boolean, @ColorInt color: Int = -1) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val statusHeight = getStatusBarHeight(statusBar.context)

            val statusBarParams = statusBar.layoutParams
            statusBarParams.height = statusHeight
            statusBar.layoutParams = statusBarParams

        }

        if (actionBarLightMode && isSupportLightMode) {
            statusBar.setBackgroundColor(if (color <= 0) -0x1 else color)//0x33000000
        } else {
            statusBar.setBackgroundColor(0x33000000)//0x33000000
        }
    }

    fun setLightStatusBar(window: Window, lightStatusBar: Boolean) {
        IMPL.setLightStatusBar(window, lightStatusBar)
    }

    private class MLightStatusBarImpl : ILightStatusBar {

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        override fun setLightStatusBar(window: Window, lightStatusBar: Boolean) {
            // 设置浅色状态栏时的界面显示
            val decor = window.decorView
            var ui = decor.systemUiVisibility
            ui = if (lightStatusBar) {
                ui or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            } else {
                ui and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            }
            decor.systemUiVisibility = ui

        }
    }

    private class MIUILightStatusBarImpl : ILightStatusBar {

        override fun setLightStatusBar(window: Window, lightStatusBar: Boolean) {
            val clazz = window.javaClass
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val decor = window.decorView
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                    var ui = when {
                        lightStatusBar -> decor.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                        else -> decor.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
                    }
                    decor.systemUiVisibility = ui
                }
                val layoutParams = Class.forName("android.view.MiuiWindowManager\$LayoutParams")
                val field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE")
                val darkModeFlag = field.getInt(layoutParams)
                val extraFlagField = clazz.getMethod("setExtraFlags", Int::class.javaPrimitiveType, Int::class.javaPrimitiveType)
                extraFlagField.invoke(window, if (lightStatusBar) darkModeFlag else 0, darkModeFlag)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        companion object {

            private const val KEY_MIUI_VERSION_CODE = "ro.miui.ui.version.code"
            private const val KEY_MIUI_VERSION_NAME = "ro.miui.ui.version.name"
            private const val KEY_MIUI_INTERNAL_STORAGE = "ro.miui.internal.storage"

            internal// ignore all exception
            val isMe: Boolean
                get() {
                    var fis: FileInputStream? = null
                    try {
                        fis = FileInputStream(File(Environment.getRootDirectory(), "build.prop"))
                        val prop = Properties()
                        prop.load(fis)
                        return (prop.getProperty(KEY_MIUI_VERSION_CODE) != null
                                || prop.getProperty(KEY_MIUI_VERSION_NAME) != null
                                || prop.getProperty(KEY_MIUI_INTERNAL_STORAGE) != null)
                    } catch (e: IOException) {
                        return false
                    } finally {
                        if (fis != null) {
                            try {
                                fis.close()
                            } catch (e: IOException) {
                            }

                        }
                    }
                }
        }
    }

    private class MeizuLightStatusBarImpl : ILightStatusBar {

        override fun setLightStatusBar(window: Window, lightStatusBar: Boolean) {
            val params = window.attributes
            try {
                val darkFlag = WindowManager.LayoutParams::class.java.getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON")
                val meizuFlags = WindowManager.LayoutParams::class.java.getDeclaredField("meizuFlags")
                darkFlag.isAccessible = true
                meizuFlags.isAccessible = true
                val bit = darkFlag.getInt(null)
                var value = meizuFlags.getInt(params)
                value = if (lightStatusBar) {
                    value or bit
                } else {
                    value and bit.inv()
                }
                meizuFlags.setInt(params, value)
                window.attributes = params
                darkFlag.isAccessible = false
                meizuFlags.isAccessible = false
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        companion object {
            internal val isMe: Boolean
                get() = Build.DISPLAY.startsWith("Flyme")
        }
    }
}
