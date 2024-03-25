package musicplayer.playmusic.audioplayer.base

import android.os.Bundle
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import dev.android.player.business.service.Session
import dev.android.player.framework.base.BaseFragment
import dev.android.player.framework.data.model.Song
import dev.android.player.framework.rx.add
import dev.android.player.framework.utils.AndroidUtil
import dev.android.player.framework.utils.ScreenUtil
import dev.android.player.framework.utils.dp
import dev.android.player.widget.onAttachLayoutAnim
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.BackpressureStrategy
import musicplayer.playmusic.audioplayer.base.databinding.FragmentBaseToolbarSpecialListBinding


/**
 * 简单的就一个Toolbar 和 一个列表
 */
open class BaseToolbarSpecialListFragment : BaseFragment() {

    private var _binding: FragmentBaseToolbarSpecialListBinding? = null
    protected val mBinding: FragmentBaseToolbarSpecialListBinding get() = _binding!!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentBaseToolbarSpecialListBinding.inflate(inflater, container, false)
        (activity as? AppCompatActivity)?.setSupportActionBar(mBinding.toolbar)
        (activity as? AppCompatActivity)?.supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
        }
        mBinding.toolbar.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            topMargin += ScreenUtil.getStatusBarHeight(context)
        }
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (getIsAnimation()) {
            mBinding.recyclerview.onAttachLayoutAnim()
        }
        Session.songPublisher
                .toFlowable(BackpressureStrategy.LATEST)
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ song: Song ->
                    adapterMusicControllerView(song.id > 0)
                }) { e: Throwable -> e.printStackTrace() }.add(this)
    }

    private fun adapterMusicControllerView(b: Boolean) {
        if (getRecyclerView().layoutParams is LinearLayout.LayoutParams) {
            getRecyclerView().updateLayoutParams<LinearLayout.LayoutParams>
            {
                bottomMargin = if (b) {
                    56.dp
                } else {
                    0
                }
            }
        } else if (getRecyclerView().layoutParams is FrameLayout.LayoutParams) {
            getRecyclerView().updateLayoutParams<FrameLayout.LayoutParams>
            {
                bottomMargin = if (b) {
                    56.dp
                } else {
                    0
                }
            }
        }
    }


    open fun getIsAnimation(): Boolean {
        return false
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
//        mBinding.toolbar.title = title ?: ""
        mBinding.collapsingToolbar.isTitleEnabled = true
        mBinding.collapsingToolbar.title = title ?: ""
    }

    protected fun setToolbarTitle(@StringRes res: Int) {
//        mBinding.toolbar.setTitle(res)
        mBinding.collapsingToolbar.isTitleEnabled = true
        var title = resources.getString(res)
        mBinding.collapsingToolbar.title = title


        try {
            val paint = TextPaint()
            paint.textSize = mBinding.collapsingToolbar.context.resources.getDimensionPixelSize(R.dimen.sp_28).toFloat() // 字体大小
            val staticLayout = StaticLayout(title, paint, ScreenUtil.getScreenWidth(mBinding.collapsingToolbar.context), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false)
            val lineCount = staticLayout.lineCount // 这就是文本行数
            mBinding.collapsingToolbar.updateLayoutParams<LinearLayout.LayoutParams> {
                height = getCollapsingToolbarHeight(lineCount == 1)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //解决4a 奇葩问题
    private fun getCollapsingToolbarHeight(isSingleLine: Boolean): Int {
        return if (isSingleLine) {
            if (AndroidUtil.isPixel4a()) {
                (142 + 56).dp
            } else {
                142.dp
            }
        } else {
            if (AndroidUtil.isPixel4a()) {
                (170 + 56).dp
            } else {
                170.dp
            }
        }
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
            inflater.inflate(R.menu.menu_special_playlist, menu)
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
    }

    override fun onViewCreatedCompat(view: View, savedInstanceState: Bundle?) {
        super.onViewCreatedCompat(view, savedInstanceState)
        mBinding.recyclerview.setPadding(0, ScreenUtil.dp2px(mActivity, 8f), 0, 0)
    }
}