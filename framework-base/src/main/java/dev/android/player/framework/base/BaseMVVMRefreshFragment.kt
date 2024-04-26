package dev.android.player.framework.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding


abstract class BaseMVVMRefreshFragment<T : ViewBinding> : BaseRefreshFragment() {


    private lateinit var isInit: LiveDataLoad

    internal class LiveDataLoad

    private var _binding: T? = null
    protected val viewBinding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initData()
        initViewModel()
    }

    abstract fun initViewModel()

    override fun onCreateViewCompat(inflater: LayoutInflater,
                                    container: ViewGroup?,
                                    savedInstanceState: Bundle?): View {
        _binding = initViewBinding(inflater)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startObserve()
        initView()
    }

    abstract fun startObserve()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    abstract fun initViewBinding(inflater: LayoutInflater): T


    protected open fun initView() {

    }


    protected open fun initData() {

    }

    /**
     * 第一次执行
     */
    protected open fun initObserve(function: () -> Unit) {
        if (!::isInit.isInitialized) {
            function()
            this.isInit = LiveDataLoad()
        }
    }

    /**
     * 非第一次执行
     */
    protected open fun unInitObserve(function: () -> Unit) {
        if (::isInit.isInitialized) {
            function()
        }
    }


}