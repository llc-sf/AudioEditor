package dev.android.player.framework.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

abstract class BaseRefreshFragment : BaseFragment(), SwipeRefreshLayout.OnRefreshListener,
        SwipeRefreshLayout.OnChildScrollUpCallback {

    private var mRefreshLayout: SwipeRefreshLayout? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        if (isEnableRefresh()) {
            val root = FrameLayout(requireContext())
            root.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            )

            mRefreshLayout = SwipeRefreshLayout(requireContext())
            mRefreshLayout?.setOnRefreshListener(this)
            mRefreshLayout?.addView(onCreateViewCompat(inflater, container, savedInstanceState))
            mRefreshLayout?.setOnChildScrollUpCallback(this)
            root.addView(mRefreshLayout,
                    ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                    )
            )
            return root
        } else {
            return onCreateViewCompat(inflater, container, savedInstanceState)
        }
    }

    /**
     * 子页面的View
     */
    abstract fun onCreateViewCompat(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View

    /**
     * 是否启用刷新布局
     */
    protected open fun isEnableRefresh(): Boolean {
        return true
    }

    protected fun getRefreshLayout() = mRefreshLayout

    /**
     * 开启下拉刷新，并启用刷新动画
     */
    protected fun onStartRefresh() {
        mRefreshLayout?.isRefreshing = true
        onRefresh()
    }

    protected fun showRefresh() {
        mRefreshLayout?.isRefreshing = true
    }

    protected fun stopRefresh() {
        mRefreshLayout?.isRefreshing = false
    }

    protected fun isRefresh() = mRefreshLayout?.isRefreshing ?: false

    override fun onRefresh() {
    }


    override fun canChildScrollUp(parent: SwipeRefreshLayout, child: View?): Boolean {
        return false
    }
}