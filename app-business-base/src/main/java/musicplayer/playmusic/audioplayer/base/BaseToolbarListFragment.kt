package musicplayer.playmusic.audioplayer.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import dev.android.player.framework.base.BaseFragment
import dev.android.player.framework.utils.ScreenUtil
import musicplayer.playmusic.audioplayer.base.databinding.FragmentBaseToolbarListBinding

/**
 * 简单的就一个Toolbar 和 一个列表
 */
open class BaseToolbarListFragment : BaseFragment() {

    private var _binding: FragmentBaseToolbarListBinding? = null
    private val mBinding: FragmentBaseToolbarListBinding get() = _binding!!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentBaseToolbarListBinding.inflate(inflater, container, false)
        (activity as? AppCompatActivity)?.setSupportActionBar(mBinding.toolbar)
        (activity as? AppCompatActivity)?.supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setTitle("")
        }
        mBinding.toolbar.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            topMargin += ScreenUtil.getStatusBarHeight(context)
        }
        return mBinding.root
    }

    /**
     * 设置Adapter 数据
     */
    protected fun setAdapter(adapter: RecyclerView.Adapter<*>) {
        mBinding.recyclerview.adapter = adapter
    }


    protected fun getRecyclerView(): RecyclerView {
        return mBinding.recyclerview
    }

    /**
     * 设置标题栏标题
     */
    protected fun setToolbarTitle(title: String?) {
        mBinding.toolbar.title = title ?: ""
    }

    protected fun setToolbarTitle(@StringRes res: Int) {
        mBinding.toolbar.setTitle(res)
    }

    /**
     * 设置标题栏子标题
     */
    protected fun setToolbarSubTitle(title: String?) {
        mBinding.toolbar.subtitle = title ?: ""
    }

    protected fun setToolbarSubTitle(@StringRes res: Int) {
        mBinding.toolbar.setSubtitle(res)
    }

    /**
     * 添加固定的头部视图
     * @param view
     */
    protected fun addHeaderView(view: View) {
        val params = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        mBinding.fixedHeaderContainer.removeAllViews()
        mBinding.fixedHeaderContainer.addView(view, params)
    }


    //是否默认加载Toolbar上搜索图标
    protected open fun isHasSearchAction() = true


    override fun onDestroyView() {
        super.onDestroyView()
        (activity as? AppCompatActivity)?.setSupportActionBar(null)
        _binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if (isHasSearchAction()) {
            inflater.inflate(R.menu.menu_search, menu)
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
    }
}