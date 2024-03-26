package dev.android.player.framework.base

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewTreeObserver
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import com.android.language.GlobalLanguages
import dev.android.player.framework.utils.AndroidUtil
import dev.android.player.framework.utils.LightStatusBarCompat


/**
 * 基础Activity
 */
open class BaseActivity : AppCompatActivity() {


    private var mRootView: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        //如果从外部更改了存储权限配置，则重新启动应用
        if (savedInstanceState != null && !AndroidUtil.hasStoragePermissionAdapter33(this)) {
            try {
                val resolveIntent = Intent(Intent.ACTION_MAIN, null)
                resolveIntent.setPackage(packageName)
                val intent = Intent(Intent.ACTION_MAIN).apply {
                    val apps = packageManager.queryIntentActivities(resolveIntent, 0)
                    val ri = apps.iterator().next()
                    if (ri != null) {
                        component = ComponentName(ri.activityInfo.packageName, ri.activityInfo.name)
                    }
                }
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
                finish()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        //目的是监听软键盘的隐藏
        window.decorView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            private var dd = -9527
            override fun onGlobalLayout() {
                if (isFinishing || isDestroyed) return
                val r = Rect()
                //r will be populated with the coordinates of your view that area still visible.
                window.decorView.getWindowVisibleDisplayFrame(r)
                //get screen height and calculate the difference with the useable area from the r
                val height: Int = resources.displayMetrics.heightPixels
                val diff = height - r.bottom
                if (dd == diff) return
                dd = diff
                if (diff <= 0) {
                    adapterNavigationBar()
                }
            }
        })
        if (!supportWindowAnimation()) return

        if (isUseBottomAnim()) {
            bottomPageAnimation(true)
        }

    }

    /**
     * 是否显示底部虚拟键导航栏
     */
    open fun isShowBottomNavigationBar() = false

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(GlobalLanguages.getLocaleContext(base))
    }

    override fun setContentView(id: Int) {
        setContentView(LayoutInflater.from(this).inflate(id, null))
    }

    override fun setContentView(view: View?) {
        mRootView = onContentViewTransform(view)
        super.setContentView(mRootView)
    }

    override fun onStart() {
        adapterNavigationBar()
        val isDarkMode = AndroidUtil.isDarkMode(this)
        LightStatusBarCompat.setLightStatusBar(window, !isDarkMode)
        ViewCompat.getWindowInsetsController(window.decorView)?.apply {
            isAppearanceLightNavigationBars = !isDarkMode
            isAppearanceLightStatusBars = !isDarkMode
        }
        super.onStart()
    }

    fun getContext() = this

    fun getRootView() = mRootView

    /**
     * 是否使用BottomAnimation
     */
    open protected fun isUseBottomAnim() = false


    /**
     * 是否启动Window动画
     */
    open protected fun supportWindowAnimation() = true


    override fun finish() {
        super.finish()
        if (!supportWindowAnimation()) return

        if (isUseBottomAnim()) {
            bottomPageAnimation(false)
        }
    }


    private fun bottomPageAnimation(start: Boolean) {
        if (start)
            overridePendingTransition(R.anim.default_page_bottom_in, R.anim.default_page_fade_out)
        else
            overridePendingTransition(R.anim.default_page_fade_in, R.anim.default_page_bottom_out)
    }

    protected open fun isEnableContentViewTransform() = true

    /**
     * 对ContentView做变换，后续如果增加底部广告，使用这个方法可以返回一个底部带带广告的视图UI
     */
    protected open fun onContentViewTransform(view: View?): View? {
        return if (isEnableContentViewTransform()) ContentViewTransformProvider.getContentViewView(this, view) else view
    }


    /**
     * 是否允许字体放缩
     */
    private fun isEnableFontScale() = false

    override fun onConfigurationChanged(newConfig: Configuration) {
        if (!isEnableFontScale()) {
            if (newConfig.fontScale != 1f) {
                onUpdateFontScaleConfig(resources)
            }
        }
        super.onConfigurationChanged(newConfig)
    }

    override fun getResources(): Resources {
        if (!isEnableFontScale()) {
            return super.getResources()
        } else {
            val res = super.getResources()
            if (res.configuration.fontScale != 1f) {
                onUpdateFontScaleConfig(res)
            }
            return res
        }

    }

    private fun onUpdateFontScaleConfig(resources: Resources) {
        val configuration = resources.configuration
        configuration.fontScale = 1.0f
        resources.updateConfiguration(configuration, resources.displayMetrics)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //默认处理
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if(hasFocus){
            adapterNavigationBar()
        }
    }

    /**
     * 适配底部导航栏
     */
    open fun adapterNavigationBar() {
        if (isShowBottomNavigationBar()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR //请求状态栏以与浅色状态栏背景兼容的模式绘制。
            } else {
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            }
        } else {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        }
    }
}