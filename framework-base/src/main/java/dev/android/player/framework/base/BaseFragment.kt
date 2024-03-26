package dev.android.player.framework.base

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment

/**
 * 实现懒加载
 */
abstract class BaseFragment : Fragment() {

    //视图是否创建完成
    private var isViewCreated = false

    //是否已经完成了数据加载
    private var isFirstLoaded = true

    //页面是否对用户可见
    private var isVisibleToUser = false

    private var isFragmentToolbar = false
    private val TAG = "BaseFragment"

    private var isBackPressedEnable = false

    protected val onBackPressedCallback: OnBackPressedCallback by lazy {
        object : OnBackPressedCallback(isBackPressedEnable) {
            override fun handleOnBackPressed() {
                onBackPressedHandler()
            }
        }
    }

    protected var mActivity: AppCompatActivity? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.mActivity = context as? AppCompatActivity
        if (activity is AppCompatActivity) {
            (activity as AppCompatActivity).onBackPressedDispatcher.addCallback(
                this,
                onBackPressedCallback
            )
        }
    }

    protected fun setFragmentToolbar(toolbar: Toolbar, isHomeAsUp: Boolean = true) {
        isFragmentToolbar = true
        (activity as? AppCompatActivity)?.setSupportActionBar(toolbar)
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayShowTitleEnabled(false)
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(isHomeAsUp)
    }

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onViewCreatedCompat(view, savedInstanceState)
        isViewCreated = true
        onLoadData()
    }

    /**
     * 子类调用需要实现的,如果实现这个，必须低调用 super.onViewCreated
     */
    protected open fun onViewCreatedCompat(view: View, savedInstanceState: Bundle?) {

    }


    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        this.isVisibleToUser = isVisibleToUser
        if (isVisibleToUser && isViewCreated) {
            onLoadData()
        }
    }

    override fun onResume() {
        super.onResume()
        onLoadData()
    }

    private fun onLoadData() {
        if (isViewCreated && (isVisibleToUser || isResumed) && isFirstLoaded) {
            isFirstLoaded = false
            onLazyLoad()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (isFragmentToolbar) {
            (activity as? AppCompatActivity)?.setSupportActionBar(null)
        }
        onResetStatus()
    }

    /**
     * 重置页面状态
     */
    private fun onResetStatus() {
        //视图是否创建完成
        isViewCreated = false
        //是否已经完成了数据加载
        isFirstLoaded = true
        //页面是否对用户可见
        isVisibleToUser = false
    }


    protected open fun onLazyLoad() {

    }

    protected open fun onBackPressedHandler() {

    }

    protected fun setBackPressedEnable(enable: Boolean) {
        isBackPressedEnable = enable
        onBackPressedCallback?.isEnabled = enable
    }
}